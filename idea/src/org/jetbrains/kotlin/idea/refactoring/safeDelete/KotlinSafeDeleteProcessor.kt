/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.refactoring.safeDelete

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Conditions
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.search.ConstructorReferencesSearchHelper
import com.intellij.psi.search.SearchRequestCollector
import com.intellij.psi.search.SearchSession
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.safeDelete.JavaSafeDeleteProcessor
import com.intellij.refactoring.safeDelete.NonCodeUsageSearchInfo
import com.intellij.refactoring.safeDelete.usageInfo.*
import com.intellij.usageView.UsageInfo
import com.intellij.util.Processor
import org.jetbrains.kotlin.asJava.*
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.lexer.JetTokens
import org.jetbrains.kotlin.idea.JetBundle
import org.jetbrains.kotlin.idea.refactoring.JetRefactoringUtil
import org.jetbrains.kotlin.idea.references.JetReference
import java.util.*
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.deleteElementAndCleanParent
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.search.usagesSearch.processDelegationCallConstructorUsages

public class KotlinSafeDeleteProcessor : JavaSafeDeleteProcessor() {
    override fun handlesElement(element: PsiElement): Boolean = element.canDeleteElement()

    override fun findUsages(
            element: PsiElement, allElementsToDelete: Array<out PsiElement>, usages: MutableList<UsageInfo>
    ): NonCodeUsageSearchInfo {
        val deleteList = allElementsToDelete.toList()

        fun getIgnoranceCondition(): Condition<PsiElement> {
            return object : Condition<PsiElement> {
                override fun value(t: PsiElement?): Boolean {
                    if (t is JetFile) return false
                    return deleteList.any { element -> JavaSafeDeleteProcessor.isInside(t, element.unwrapped) }
                }
            }
        }

        fun getSearchInfo(element: PsiElement): NonCodeUsageSearchInfo {
            return NonCodeUsageSearchInfo(getIgnoranceCondition(), element)
        }

        fun findUsagesByJavaProcessor(element: PsiElement, forceReferencedElementUnwrapping: Boolean): NonCodeUsageSearchInfo? {
            val javaUsages = ArrayList<UsageInfo>()
            val searchInfo = super.findUsages(element, allElementsToDelete, javaUsages)

            javaUsages.map { usageInfo ->
                when (usageInfo) {
                    is SafeDeleteOverridingMethodUsageInfo ->
                        usageInfo.getSmartPointer().getElement()?.let { usageElement ->
                            KotlinSafeDeleteOverridingUsageInfo(usageElement, usageInfo.getReferencedElement())
                        }

                    is SafeDeleteOverrideAnnotation ->
                        usageInfo.getSmartPointer().getElement()?.let { usageElement ->
                            if (usageElement.toLightMethods().all { method -> method.findSuperMethods().size() == 0 }) {
                                KotlinSafeDeleteOverrideAnnotation(usageElement, usageInfo.getReferencedElement())
                            }
                            else null
                        }

                    is SafeDeleteReferenceJavaDeleteUsageInfo ->
                        usageInfo.getElement()?.let { usageElement ->
                            if (usageElement.getNonStrictParentOfType<JetValueArgumentName>() != null) null
                            else {
                                usageElement.getNonStrictParentOfType<JetImportDirective>()?.let { importDirective ->
                                    SafeDeleteImportDirectiveUsageInfo(importDirective, element.unwrapped as JetDeclaration)
                                } ?: if (forceReferencedElementUnwrapping) {
                                    SafeDeleteReferenceJavaDeleteUsageInfo(usageElement, element.unwrapped, usageInfo.isSafeDelete())
                                } else usageInfo
                            }
                        }

                    else -> usageInfo
                }
            }.filterNotNull().toCollection(usages)

            return searchInfo
        }

        fun findUsagesByJavaProcessor(elements: Stream<PsiElement>, insideDeleted: Condition<PsiElement>): Condition<PsiElement> =
                elements
                        .map { element -> findUsagesByJavaProcessor(element, true)?.getInsideDeletedCondition() }
                        .filterNotNull()
                        .fold(insideDeleted) { condition1, condition2 -> Conditions.or(condition1, condition2) }

        fun findUsagesByJavaProcessor(jetDeclaration: JetDeclaration): NonCodeUsageSearchInfo {
            return NonCodeUsageSearchInfo(
                    findUsagesByJavaProcessor(
                            jetDeclaration.toLightElements().stream(),
                            getIgnoranceCondition()
                    ),
                    jetDeclaration
            )
        }

        fun findKotlinDeclarationUsages(declaration: JetDeclaration): NonCodeUsageSearchInfo {
            ReferencesSearch.search(declaration, declaration.getUseScope())
                    .stream()
                    .filterNot { reference -> getIgnoranceCondition().value(reference.getElement()) }
                    .mapTo(usages) { reference ->
                        reference.getElement().getNonStrictParentOfType<JetImportDirective>()?.let { importDirective ->
                            SafeDeleteImportDirectiveUsageInfo(importDirective, element.unwrapped as JetDeclaration)
                        } ?: SafeDeleteReferenceSimpleDeleteUsageInfo(element, declaration, false)
                    }

            return getSearchInfo(declaration)
        }

        fun findTypeParameterUsages(parameter: JetTypeParameter) {
            val owner = parameter.getNonStrictParentOfType<JetTypeParameterListOwner>()
            if (owner == null) return

            val parameterList = owner.getTypeParameters()
            val parameterIndex = parameterList.indexOf(parameter)

            for (reference in ReferencesSearch.search(owner)) {
                if (reference !is JetReference) continue

                val referencedElement = reference.getElement()

                val argList = referencedElement.getNonStrictParentOfType<JetUserType>()?.let { jetType ->
                    jetType.getTypeArgumentList()
                } ?: referencedElement.getNonStrictParentOfType<JetCallExpression>()?.let { callExpression ->
                    callExpression.getTypeArgumentList()
                } ?: null

                if (argList != null) {
                    val projections = argList.getArguments()
                    if (parameterIndex < projections.size()) {
                        usages.add(SafeDeleteTypeArgumentListUsageInfo(projections.get(parameterIndex), parameter))
                    }
                }
            }
        }

        fun findDelegationCallUsages(element: PsiElement) {
            element.processDelegationCallConstructorUsages(element.getUseScope()) {
                usages.add(SafeDeleteReferenceSimpleDeleteUsageInfo(it, element, false))
            }
        }

        return when (element) {
            is JetClassOrObject -> {
                element.toLightClass()?.let { klass ->
                    findDelegationCallUsages(klass)
                    findUsagesByJavaProcessor(klass, false)
                }
            }

            is JetSecondaryConstructor -> {
                element.getRepresentativeLightMethod()?.let { method ->
                    findDelegationCallUsages(method)
                    findUsagesByJavaProcessor(method, false)
                }
            }

            is JetNamedFunction -> {
                if (element.isLocal()) {
                    findKotlinDeclarationUsages(element)
                }
                else {
                    element.getRepresentativeLightMethod()?.let { method -> findUsagesByJavaProcessor(method, false) }
                }
            }

            is PsiMethod ->
                findUsagesByJavaProcessor(element, false)

            is JetProperty -> {
                if (element.isLocal()) {
                    findKotlinDeclarationUsages(element)
                }
                else {
                    findUsagesByJavaProcessor(element)
                }
            }

            is JetTypeParameter -> {
                findTypeParameterUsages(element)
                findUsagesByJavaProcessor(element)
            }

            is JetParameter ->
                findUsagesByJavaProcessor(element)

            else -> null
        } ?: getSearchInfo(element)
    }

    override fun findConflicts(element: PsiElement, allElementsToDelete: Array<out PsiElement>): MutableCollection<String>? {
        if (element is JetNamedFunction || element is JetProperty) {
            val jetClass = element.getNonStrictParentOfType<JetClass>()
            if (jetClass == null || jetClass.getBody() != element.getParent()) return null

            val modifierList = jetClass.getModifierList()
            if (modifierList != null && modifierList.hasModifier(JetTokens.ABSTRACT_KEYWORD)) return null

            val bindingContext = (element as JetElement).analyze()

            val declarationDescriptor = bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, element)
            if (declarationDescriptor !is CallableMemberDescriptor) return null

            return declarationDescriptor.getOverriddenDescriptors()
                    .stream()
                    .filter { overridenDescriptor -> overridenDescriptor.getModality() == Modality.ABSTRACT }
                    .mapTo(ArrayList<String>()) { overridenDescriptor ->
                        JetBundle.message(
                                "x.implements.y",
                                JetRefactoringUtil.formatFunction(declarationDescriptor, true),
                                JetRefactoringUtil.formatClass(declarationDescriptor.getContainingDeclaration(), true),
                                JetRefactoringUtil.formatFunction(overridenDescriptor, true),
                                JetRefactoringUtil.formatClass(overridenDescriptor.getContainingDeclaration(), true)
                        )
                    }
        }

        return super.findConflicts(element, allElementsToDelete)
    }

    /*
     * Mostly copied from JavaSafeDeleteProcessor.preprocessUsages
     * Revision: d4fc033
     * (replaced original dialog)
     */
    override fun preprocessUsages(project: Project, usages: Array<out UsageInfo>): Array<UsageInfo>? {
        val result = ArrayList<UsageInfo>()
        val overridingMethodUsages = ArrayList<UsageInfo>()

        for (usage in usages) {
            if (usage is KotlinSafeDeleteOverridingUsageInfo) {
                overridingMethodUsages.add(usage)
            } else {
                result.add(usage)
            }
        }

        if (!overridingMethodUsages.isEmpty()) {
            if (ApplicationManager.getApplication()!!.isUnitTestMode()) {
                result.addAll(overridingMethodUsages)
            } else {
                val dialog = KotlinOverridingDialog(project, overridingMethodUsages)
                dialog.show()

                if (!dialog.isOK()) return null

                result.addAll(dialog.getSelected())
            }
        }

        return result.copyToArray()
    }

    override fun prepareForDeletion(element: PsiElement) {
        when (element) {
            is PsiMethod -> element.cleanUpOverrides()

            is JetNamedFunction ->
                if (!element.isLocal()) {
                    element.getRepresentativeLightMethod()?.cleanUpOverrides()
                }

            is JetProperty ->
                if (!element.isLocal()) {
                    element.toLightMethods().forEach { method -> method.cleanUpOverrides() }
                }

            is JetTypeParameter ->
                element.deleteElementAndCleanParent()

            is JetParameter ->
                JetPsiUtil.deleteElementWithDelimiters(element)
        }
    }

    override fun getElementsToSearch(
            element: PsiElement, module: Module?, allElementsToDelete: Collection<PsiElement>
    ): Collection<PsiElement>? {
        when (element) {
            is JetParameter ->
                return element.toPsiParameter()?.let { psiParameter ->
                    JetRefactoringUtil.checkParametersInMethodHierarchy(psiParameter)
                } ?: Collections.singletonList(element)

            is PsiParameter ->
                return JetRefactoringUtil.checkParametersInMethodHierarchy(element)
        }

        if (ApplicationManager.getApplication()!!.isUnitTestMode()) return Collections.singletonList(element)

        return when (element) {
            is JetNamedFunction, is JetProperty ->
                JetRefactoringUtil.checkSuperMethods(
                        element as JetDeclaration, allElementsToDelete, "super.methods.delete.with.usage.search"
                )

            else -> super.getElementsToSearch(element, module, allElementsToDelete)
        }
    }
}
