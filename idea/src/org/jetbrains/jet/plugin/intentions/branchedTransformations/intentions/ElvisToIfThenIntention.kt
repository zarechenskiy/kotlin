/*
 * Copyright 2010-2014 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.intentions.branchedTransformations.intentions

import org.jetbrains.jet.lang.psi.JetBinaryExpression
import org.jetbrains.jet.plugin.intentions.JetSelfTargetingIntention
import com.intellij.openapi.editor.Editor
import org.jetbrains.jet.lexer.JetTokens
import org.jetbrains.jet.lang.psi.JetPsiUtil
import org.jetbrains.jet.plugin.intentions.branchedTransformations.convertToIfStatement
import org.jetbrains.jet.plugin.intentions.branchedTransformations.introduceValueForCondition
import org.jetbrains.jet.lang.resolve.calls.autocasts.DataFlowValueFactory
import org.jetbrains.jet.plugin.project.AnalyzerFacadeWithCache
import org.jetbrains.jet.lang.descriptors.VariableDescriptor
import org.jetbrains.jet.lang.resolve.BindingContextUtils

public class ElvisToIfThenIntention : JetSelfTargetingIntention<JetBinaryExpression>("elvis.to.if.then", javaClass()) {
    override fun isApplicableTo(element: JetBinaryExpression): Boolean =
            element.getOperationToken() == JetTokens.ELVIS

    override fun applyTo(element: JetBinaryExpression, editor: Editor) {
        val lhs = checkNotNull(JetPsiUtil.deparenthesize(element.getLeft()), "Left hand side of elvis expression cannot be null")
        val rhs = checkNotNull(JetPsiUtil.deparenthesize(element.getRight()), "Right hand side of elvis expression cannot be null")
        val context = AnalyzerFacadeWithCache.getContextForElement(lhs)
        val descriptor = BindingContextUtils.extractVariableDescriptorIfAny(context, lhs, false)

        val ifStatement = element.convertToIfStatement(lhs, lhs, rhs)
        if (!(descriptor is VariableDescriptor && DataFlowValueFactory.isStableVariable(descriptor))) {
            ifStatement.introduceValueForCondition(ifStatement.getThen()!!, element.getProject(), editor)
        }
    }

}
