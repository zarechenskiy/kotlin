/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.refactoring.rename;

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import org.jetbrains.jet.asJava.KotlinLightClass
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.OverridingMethodsSearch
import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.jet.asJava.LightClassUtil
import org.jetbrains.jet.lang.psi.JetNamedFunction
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiMirrorElement
import com.intellij.psi.SyntheticElement
import com.intellij.refactoring.util.RefactoringUtil
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.refactoring.rename.RenameJavaMethodProcessor

public class RenameKotlinFunctionProcessor : RenamePsiElementProcessor() {
    override fun canProcessElement(element: PsiElement) = (element is JetNamedFunction) || unwrapPsiMethod(element) != null

    override fun substituteElementToRename(element: PsiElement?, editor: Editor?): PsiElement?  {
        if (element is JetNamedFunction) {
            val wrappedMethod = ApplicationManager.getApplication()!!.runReadAction(Computable { LightClassUtil.getLightClassMethod(element) })
            if (wrappedMethod != null) {
                val jetFun = unwrapPsiMethod(RenameJavaMethodProcessor().substituteElementToRename(wrappedMethod, editor))
                if (jetFun != null) {
                    return jetFun
                }
            }
        }

        return unwrapPsiMethod(element) ?: super.substituteElementToRename(element, editor);
    }

    override fun prepareRenaming(element: PsiElement?, newName: String?, allRenames: MutableMap<PsiElement, String>, scope: SearchScope) {
        super.prepareRenaming(element, newName, allRenames, scope)

        if (element is JetNamedFunction) {
            val psiMethod = ApplicationManager.getApplication()!!.runReadAction(Computable { LightClassUtil.getLightClassMethod(element) })
            if (psiMethod != null) {
                OverridingMethodsSearch.search(psiMethod, scope, true)?.forEach { overrider ->
                    var overriderMethod = overrider

                    if (overriderMethod is PsiMirrorElement) {
                        val prototype = (overriderMethod as PsiMirrorElement).getPrototype()
                            if (prototype is PsiMethod) {
                                overriderMethod = prototype
                        }
                    }

                    if (!(overriderMethod is SyntheticElement)) {
                        val overriderName = overriderMethod.getName()
                        val baseName = element.getName()
                        val newOverriderName = RefactoringUtil.suggestNewOverriderName(overriderName, baseName, newName)

                        if (newOverriderName != null) {
                            val unwrappedJetFunction = unwrapPsiMethod(overriderMethod)
                            if (unwrappedJetFunction != null) {
                                allRenames[unwrappedJetFunction] = newOverriderName
                            }
                            else {
                                RenameProcessor.assertNonCompileElement(overriderMethod)
                                allRenames[overriderMethod] = newOverriderName
                            }
                        }
                    }

                    true
                }
            }
        }
    }

    private fun unwrapPsiMethod(element: PsiElement?): JetNamedFunction? {
        if (element is PsiMethod && element is PsiCompiledElement) {
            val originalElement = (element as PsiCompiledElement).getMirror()

            if (originalElement is JetNamedFunction) {
                return originalElement
            }
        }

        return null
    }
}
