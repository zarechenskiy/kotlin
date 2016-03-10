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

package org.jetbrains.kotlin.idea.formatter

import com.intellij.formatting.*
import com.intellij.formatting.alignment.AlignmentStrategy
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.KtNodeTypes.*
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.common.formatter.*
import org.jetbrains.kotlin.idea.core.formatter.KotlinCodeStyleSettings
import org.jetbrains.kotlin.lexer.KtTokens.*

private val KDOC_COMMENT_INDENT = 1

private val BINARY_EXPRESSIONS = TokenSet.create(BINARY_EXPRESSION, BINARY_WITH_TYPE, IS_EXPRESSION)
private val ALIGN_FOR_BINARY_OPERATIONS = TokenSet.create(MUL, DIV, PERC, PLUS, MINUS, ELVIS, LT, GT, LTEQ, GTEQ, ANDAND, OROR)

private val CODE_BLOCKS = TokenSet.create(BLOCK, CLASS_BODY, FUNCTION_LITERAL)

/**
 * @see Block for good JavaDoc documentation
 */
class KotlinBlock(
        node: ASTNode,
        private val myAlignmentStrategy: CommonAlignmentStrategy,
        private val myIndent: Indent?,
        wrap: Wrap?,
        private val mySettings: CodeStyleSettings,
        private val mySpacingBuilder: KotlinSpacingBuilder) : AbstractBlock(node, wrap, myAlignmentStrategy.getAlignment(node)) {

    private val kotlinDelegationBlock = object : KotlinDelegationBlock(node, mySettings, mySpacingBuilder) {
        override fun buildSubBlock(child: ASTNode, alignmentStrategy: CommonAlignmentStrategy, wrappingStrategy: WrappingStrategy): Block {
            val childWrap = wrappingStrategy.getWrap(child.elementType)

            // Skip one sub-level for operators, so type of block node is an element type of operator
            if (child.elementType === OPERATION_REFERENCE) {
                val operationNode = child.firstChildNode
                if (operationNode != null) {
                    return KotlinBlock(
                            operationNode,
                            alignmentStrategy,
                            createChildIndent(child),
                            childWrap,
                            mySettings,
                            mySpacingBuilder)
                }
            }

            return KotlinBlock(child, alignmentStrategy, createChildIndent(child), childWrap, mySettings, mySpacingBuilder)
        }

        override fun getChildrenAlignmentStrategy(): CommonAlignmentStrategy {
            val jetCommonSettings = mySettings.getCommonSettings(KotlinLanguage.INSTANCE)
            val jetSettings = mySettings.getCustomSettings(KotlinCodeStyleSettings::class.java)
            val parentType = myNode.elementType
            if (parentType === VALUE_PARAMETER_LIST) {
                return getAlignmentForChildInParenthesis(
                        jetCommonSettings.ALIGN_MULTILINE_PARAMETERS, VALUE_PARAMETER, COMMA,
                        jetCommonSettings.ALIGN_MULTILINE_METHOD_BRACKETS, LPAR, RPAR)
            }
            else if (parentType === VALUE_ARGUMENT_LIST) {
                return getAlignmentForChildInParenthesis(
                        jetCommonSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS, VALUE_ARGUMENT, COMMA,
                        jetCommonSettings.ALIGN_MULTILINE_METHOD_BRACKETS, LPAR, RPAR)
            }
            else if (parentType === WHEN) {
                return getAlignmentForCaseBranch(jetSettings.ALIGN_IN_COLUMNS_CASE_BRANCH)
            }
            else if (parentType === WHEN_ENTRY) {
                return myAlignmentStrategy
            }
            else if (parentType in BINARY_EXPRESSIONS && getOperationType(node) in ALIGN_FOR_BINARY_OPERATIONS) {
                return NodeAlignmentStrategy.fromTypes(AlignmentStrategy.wrap(
                        createAlignment(jetCommonSettings.ALIGN_MULTILINE_BINARY_OPERATION, alignment)))
            }
            else if (parentType === SUPER_TYPE_LIST || parentType === INITIALIZER_LIST) {
                return NodeAlignmentStrategy.fromTypes(AlignmentStrategy.wrap(
                        createAlignment(jetCommonSettings.ALIGN_MULTILINE_EXTENDS_LIST, alignment)))
            }
            else if (parentType === PARENTHESIZED) {
                return object : NodeAlignmentStrategy() {
                    internal var bracketsAlignment: Alignment? = if (jetCommonSettings.ALIGN_MULTILINE_BINARY_OPERATION) Alignment.createAlignment() else null

                    override fun getAlignment(childNode: ASTNode): Alignment? {
                        val childNodeType = childNode.elementType
                        val prev = getPrevWithoutWhitespace(childNode)

                        if (prev != null && prev.elementType === TokenType.ERROR_ELEMENT || childNodeType === TokenType.ERROR_ELEMENT) {
                            return bracketsAlignment
                        }

                        if (childNodeType === LPAR || childNodeType === RPAR) {
                            return bracketsAlignment
                        }

                        return null
                    }
                }
            }

            return NodeAlignmentStrategy.getNullStrategy()
        }

        override fun createSyntheticSpacingNodeBlock(node: ASTNode): ASTBlock {
            return object : AbstractBlock(node, null, null) {
                override fun isLeaf(): Boolean = false
                override fun getSpacing(child1: Block?, child2: Block): Spacing? = null
                override fun buildChildren(): List<Block> = emptyList()
            }
        }
    }

    override fun getIndent(): Indent? = myIndent

    override fun buildChildren(): List<Block> = kotlinDelegationBlock.buildChildren()

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = mySpacingBuilder.getSpacing(this, child1, child2)

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val type = node.elementType
        return when (type) {
            in CODE_BLOCKS, WHEN, IF, FOR, WHILE, DO_WHILE -> ChildAttributes(Indent.getNormalIndent(), null)

            TRY -> ChildAttributes(Indent.getNoneIndent(), null)

            DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION -> ChildAttributes(Indent.getContinuationWithoutFirstIndent(), null)

            VALUE_PARAMETER_LIST, VALUE_ARGUMENT_LIST -> {
                if (newChildIndex != 1 && newChildIndex != 0 && newChildIndex < subBlocks.size) {
                    val block = subBlocks[newChildIndex]
                    ChildAttributes(block.indent, block.alignment)
                }
                else {
                    ChildAttributes(Indent.getContinuationIndent(), null)
                }
            }

            DOC_COMMENT -> ChildAttributes(Indent.getSpaceIndent(KDOC_COMMENT_INDENT), null)

            PARENTHESIZED -> super.getChildAttributes(newChildIndex)

            else -> {
                val blocks = subBlocks
                if (newChildIndex != 0) {
                    val isIncomplete = if (newChildIndex < blocks.size) blocks[newChildIndex - 1].isIncomplete else isIncomplete
                    if (isIncomplete) {
                        return super.getChildAttributes(newChildIndex)
                    }
                }

                ChildAttributes(Indent.getNoneIndent(), null)
            }
        }
    }

    override fun isLeaf(): Boolean = kotlinDelegationBlock.isLeaf()
}

private fun getPrevWithoutWhitespace(pNode: ASTNode?): ASTNode? {
    var node = pNode
    node = node!!.treePrev
    while (node != null && node.elementType === TokenType.WHITE_SPACE) {
        node = node.treePrev
    }

    return node
}

private fun getAlignmentForChildInParenthesis(
        shouldAlignChild: Boolean, parameter: IElementType, delimiter: IElementType,
        shouldAlignParenthesis: Boolean, openBracket: IElementType, closeBracket: IElementType): NodeAlignmentStrategy {
    val parameterAlignment = if (shouldAlignChild) Alignment.createAlignment() else null
    val bracketsAlignment = if (shouldAlignParenthesis) Alignment.createAlignment() else null

    return object : NodeAlignmentStrategy() {
        override fun getAlignment(node: ASTNode): Alignment? {
            val childNodeType = node.elementType

            val prev = getPrevWithoutWhitespace(node)
            if (prev != null && prev.elementType === TokenType.ERROR_ELEMENT || childNodeType === TokenType.ERROR_ELEMENT) {
                // Prefer align to parameters on incomplete code (case of line break after comma, when next parameters is absent)
                return parameterAlignment
            }

            if (childNodeType === openBracket || childNodeType === closeBracket) {
                return bracketsAlignment
            }

            if (childNodeType === parameter || childNodeType === delimiter) {
                return parameterAlignment
            }

            return null
        }
    }
}

private fun getAlignmentForCaseBranch(shouldAlignInColumns: Boolean): NodeAlignmentStrategy {
    return if (shouldAlignInColumns) {
        NodeAlignmentStrategy.fromTypes(
                AlignmentStrategy.createAlignmentPerTypeStrategy(listOf(ARROW as IElementType), WHEN_ENTRY, true))
    }
    else {
        NodeAlignmentStrategy.getNullStrategy()
    }
}

private fun createAlignment(alignOption: Boolean, defaultAlignment: Alignment?): Alignment? {
    return if (alignOption) createAlignmentOrDefault(null, defaultAlignment) else defaultAlignment
}

private fun createAlignmentOrDefault(base: Alignment?, defaultAlignment: Alignment?): Alignment? {
    return defaultAlignment ?: if (base == null) Alignment.createAlignment() else Alignment.createChildAlignment(base)
}

private fun getOperationType(node: ASTNode): IElementType? = node.findChildByType(OPERATION_REFERENCE)?.elementType