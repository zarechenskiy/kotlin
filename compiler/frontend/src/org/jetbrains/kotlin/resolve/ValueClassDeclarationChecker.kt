/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.SimpleDeclarationChecker

object ValueClassDeclarationChecker : SimpleDeclarationChecker {
    override fun check(
            declaration: KtDeclaration,
            descriptor: DeclarationDescriptor,
            diagnosticHolder: DiagnosticSink,
            bindingContext: BindingContext) {

        if (declaration !is KtClass) return
        if (descriptor !is ClassDescriptor) return

        if (!descriptor.isValue) return

        val containingDeclaration = descriptor.containingDeclaration
        if (containingDeclaration is ClassDescriptor && containingDeclaration.kind != ClassKind.OBJECT) {
            diagnosticHolder.report(Errors.VALUE_CLASS_NOT_TOP_LEVEL_OR_OBJECT.on(declaration))
        }

        if (descriptor.unsubstitutedPrimaryConstructor == null) {
            declaration.nameIdentifier?.let { diagnosticHolder.report(Errors.PRIMARY_CONSTRUCTOR_REQUIRED_FOR_VALUE_CLASS.on(it)) }
        }

        val primaryConstructor = declaration.getPrimaryConstructor()
        val parameters = primaryConstructor?.valueParameters ?: emptyList()
        if (parameters.size != 1) {
            (primaryConstructor?.valueParameterList ?: declaration.nameIdentifier)?.let {
                diagnosticHolder.report(Errors.VALUE_CLASS_WRONG_PARAMETERS_SIZE.on(it))
            }
        }

        for (parameter in parameters) {
            if (!parameter.hasValOrVar()) {
                diagnosticHolder.report(Errors.VALUE_CLASS_NOT_PROPERTY_PARAMETER.on(parameter))
            }

            if (parameter.isMutable) {
                diagnosticHolder.report(Errors.VALUE_CLASS_NOT_IMMUTABLE_PARAMETER.on(parameter))
            }
        }
    }

}