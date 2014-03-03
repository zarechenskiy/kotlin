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

package org.jetbrains.jet.plugin.intentions.branchedTransformations

import org.jetbrains.jet.lang.psi.JetExpression
import org.jetbrains.jet.lang.psi.JetBlockExpression
import org.jetbrains.jet.lang.psi.JetBinaryExpression
import org.jetbrains.jet.lang.psi.JetIfExpression
import org.jetbrains.jet.lang.psi.JetPsiUtil
import org.jetbrains.jet.lexer.JetTokens
import org.jetbrains.jet.lang.psi.JetPsiFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.jet.plugin.refactoring.introduceVariable.JetIntroduceVariableHandler
import org.jetbrains.jet.lang.psi.JetSafeQualifiedExpression
import org.jetbrains.jet.lang.resolve.BindingContext
import org.jetbrains.jet.lang.resolve.BindingContext.EXPECTED_EXPRESSION_TYPE
import org.jetbrains.jet.plugin.project.AnalyzerFacadeWithCache
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns

fun JetBinaryExpression.comparesNonNullToNull(): Boolean {
    val operationToken = this.getOperationToken()
    val rhs = this.getRight()
    val lhs = this.getLeft()
    if (rhs == null || lhs == null) return false

    val rightIsNull = rhs.evaluatesToNull()
    val leftIsNull = lhs.evaluatesToNull()
    return leftIsNull != rightIsNull && (operationToken == JetTokens.EQEQ || operationToken == JetTokens.EXCLEQ)
}

fun JetExpression.getExpressionFromClause(): JetExpression? {
    val innerExpression = JetPsiUtil.deparenthesize(this)
    return when (innerExpression) {
        is JetBlockExpression ->
            if (innerExpression.getStatements().size() == 1)
                return JetPsiUtil.deparenthesize(innerExpression.getStatements().first as? JetExpression)
            else null

        null -> null

        else -> innerExpression
    }

}

fun JetSafeQualifiedExpression.returnValueIsChecked(context: BindingContext): Boolean {
    val expectedType = context.get(BindingContext.EXPECTED_EXPRESSION_TYPE, this);
    val isUnit = expectedType != null && KotlinBuiltIns.getInstance().isUnit(expectedType);
    // Some "statements" are actually expressions returned from lambdas, their expected types are non-null
    val isStatement = context.get(BindingContext.STATEMENT, this) == true && expectedType == null;

    return !isUnit && !isStatement
}

fun JetSafeQualifiedExpression.isStatement(): Boolean {
    val context = AnalyzerFacadeWithCache.getContextForElement(this)
    val isStatement = context.get(BindingContext.STATEMENT, this) ?: false
    return isStatement
}

fun JetBinaryExpression.getNonNullExpression(): JetExpression? = when {
    this.getLeft()?.evaluatesToNull() == false -> this.getLeft()
    this.getRight()?.evaluatesToNull() == false -> this.getRight()
    else -> null
}

fun JetExpression.evaluatesToNull(): Boolean = this.getExpressionFromClause()?.getText() == "null"

fun JetExpression.isNullOrEmpty(): Boolean = this.evaluatesToNull() || this is JetBlockExpression && this.getStatements().empty

fun JetExpression.doesNotEvaluateToNullOrUnit(): Boolean {
    val innerExpression = this.getExpressionFromClause()
    return innerExpression != null && innerExpression.getText() != "null"
}

fun JetExpression.evaluatesTo(other: JetExpression): Boolean {
    return this.getExpressionFromClause()?.getText() == other.getText()
}

fun JetExpression.convertToIfStatement(conditionLhs: JetExpression, thenClause: JetExpression, elseClause: JetExpression?): JetIfExpression {

    val elseBranch = if (elseClause == null) "" else " else ${elseClause.getText()}"
    val conditionalString = "if (${conditionLhs.getText()} != null) ${thenClause.getText()}$elseBranch"

    val resultingExpression = JetPsiFactory.createExpression(this.getProject(), conditionalString)
    val st = this.replace(resultingExpression) as JetExpression
    return JetPsiUtil.deparenthesize(st) as JetIfExpression
}

fun JetIfExpression.introduceValueForCondition(occurrenceInThenClause: JetExpression, project: Project, editor: Editor) {
    val occurrenceInConditional = (this.getCondition() as JetBinaryExpression).getLeft()!!
    JetIntroduceVariableHandler.doRefactoring(project, editor, occurrenceInConditional, listOf(occurrenceInConditional, occurrenceInThenClause))
}

