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

import org.jetbrains.jet.plugin.intentions.JetSelfTargetingIntention
import com.intellij.openapi.editor.Editor
import org.jetbrains.jet.lang.psi.JetPsiFactory
import org.jetbrains.jet.lang.psi.JetExpression
import org.jetbrains.jet.lang.psi.JetIfExpression
import org.jetbrains.jet.lang.psi.JetBinaryExpression
import org.jetbrains.jet.lexer.JetTokens
import org.jetbrains.jet.plugin.intentions.branchedTransformations.getExpressionFromClause
import org.jetbrains.jet.plugin.intentions.branchedTransformations.evaluatesTo
import org.jetbrains.jet.plugin.intentions.branchedTransformations.comparesNonNullToNull
import org.jetbrains.jet.plugin.intentions.branchedTransformations.getNonNullExpression
import org.jetbrains.jet.plugin.intentions.branchedTransformations.doesNotEvaluateToNullOrUnit
import org.jetbrains.jet.plugin.intentions.branchedTransformations.replace

public class IfThenToElvisIntention : JetSelfTargetingIntention<JetIfExpression>("if.then.to.elvis", javaClass()) {

    override fun isApplicableTo(element: JetIfExpression): Boolean {
        val condition = element.getCondition()
        val thenClause = element.getThen()
        val elseClause = element.getElse()
        if (thenClause == null || elseClause == null || condition !is JetBinaryExpression || !condition.comparesNonNullToNull()) return false

        val expression = condition.getNonNullExpression()
        if (expression == null) return false

        return when (condition.getOperationToken()) {
                   JetTokens.EQEQ -> thenClause.doesNotEvaluateToNullOrUnit() && elseClause.evaluatesTo(expression)
                   JetTokens.EXCLEQ -> elseClause.doesNotEvaluateToNullOrUnit() && thenClause.evaluatesTo(expression)
                   else -> false
               }
    }

    private data class Elvis(val lhs: JetExpression, val rhs: JetExpression)

    override fun applyTo(element: JetIfExpression, editor: Editor) {
        val condition = element.getCondition() as JetBinaryExpression

        val thenClause = checkNotNull(element.getThen(), "The then clause cannot be null")
        val elseClause = checkNotNull(element.getElse(), "The else clause cannot be null")
        val thenExpression = checkNotNull(thenClause.getExpressionFromClause(), "Then clause must contain expression")
        val elseExpression = checkNotNull(elseClause.getExpressionFromClause(), "Else clause must contain expression")

        val (lhs, rhs) =
                when(condition.getOperationToken()) {
                    JetTokens.EQEQ -> Elvis(elseExpression, thenExpression)
                    JetTokens.EXCLEQ -> Elvis(thenExpression, elseExpression)
                    else -> throw IllegalStateException("Operation token must be either null or not null")
                }

        val resultingExprString = "${lhs.getText()} ?: ${rhs.getText()}"
        element.replace(resultingExprString)
    }
}
