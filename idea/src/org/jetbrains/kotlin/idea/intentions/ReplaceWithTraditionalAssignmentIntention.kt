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

package org.jetbrains.kotlin.idea.intentions

import org.jetbrains.kotlin.psi.JetBinaryExpression
import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.psi.JetSimpleNameExpression
import org.jetbrains.kotlin.lexer.JetTokens
import org.jetbrains.kotlin.psi.JetPsiFactory
import org.jetbrains.kotlin.psi.JetPsiUnparsingUtils

public class ReplaceWithTraditionalAssignmentIntention : JetSelfTargetingOffsetIndependentIntention<JetBinaryExpression>("replace.with.traditional.assignment.intention", javaClass()) {
    override fun isApplicableTo(element: JetBinaryExpression): Boolean {
        fun checkForNullSafety(element: JetBinaryExpression): Boolean = element.getLeft() != null && element.getRight() != null && element.getOperationToken() != null

        fun checkValidity(element: JetBinaryExpression): Boolean {
            return element.getLeft() is JetSimpleNameExpression &&
                    JetTokens.AUGMENTED_ASSIGNMENTS.contains(element.getOperationToken())
        }

        return checkForNullSafety(element) && checkValidity(element)
    }

    override fun applyTo(element: JetBinaryExpression, editor: Editor) {
        fun buildReplacement(element: JetBinaryExpression): String {
            val replacementStringBuilder = StringBuilder("${element.getLeft()!!.getText()} = ${element.getLeft()!!.getText()} ")

            replacementStringBuilder.append(
                    when {
                        element.getOperationToken() == JetTokens.PLUSEQ -> "+"
                        element.getOperationToken() == JetTokens.MINUSEQ -> "-"
                        element.getOperationToken() == JetTokens.MULTEQ -> "*"
                        element.getOperationToken() == JetTokens.DIVEQ -> "/"
                        element.getOperationToken() == JetTokens.PERC -> "%"
                        else -> ""
                    }
            ).append(" ${JetPsiUnparsingUtils.parenthesizeIfNeeded(element.getRight())}")

            return replacementStringBuilder.toString()
        }

        element.replace(JetPsiFactory(element).createExpression(buildReplacement(element)))
    }
}
