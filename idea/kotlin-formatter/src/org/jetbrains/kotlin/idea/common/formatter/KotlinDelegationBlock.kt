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

/**
 * Created by Mikhail.Zarechenskiy on 3/10/2016.
 */
package org.jetbrains.kotlin.idea.common.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.containers.ContainerUtil
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtDeclaration
import java.util.*
import org.jetbrains.kotlin.idea.common.formatter.NodeIndentStrategy.strategy
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes

private val QUALIFIED_OPERATION = TokenSet.create(KtTokens.DOT, KtTokens.SAFE_ACCESS)
private val KDOC_COMMENT_INDENT = 1

private val BINARY_EXPRESSIONS = TokenSet.create(KtNodeTypes.BINARY_EXPRESSION, KtNodeTypes.BINARY_WITH_TYPE, KtNodeTypes.IS_EXPRESSION)
private val KDOC_CONTENT = TokenSet.create(KDocTokens.KDOC, KDocElementTypes.KDOC_SECTION, KDocElementTypes.KDOC_TAG)

abstract class KotlinDelegationBlock(
        private val node: ASTNode,
        private val settings: CodeStyleSettings,
        private val spacingBuilder: KotlinSpacingBuilder) {
    private @Volatile var mySubBlocks: List<Block>? = null

    abstract protected fun buildSubBlock(child: ASTNode, alignmentStrategy: CommonAlignmentStrategy, wrappingStrategy: WrappingStrategy): Block

    abstract protected fun getChildrenAlignmentStrategy(): CommonAlignmentStrategy

    abstract protected fun createSyntheticSpacingNodeBlock(node: ASTNode): ASTBlock

    fun isLeaf(): Boolean = node.firstChildNode == null

    fun buildChildren(): List<Block> {
        if (mySubBlocks == null) {
            var nodeSubBlocks = buildSubBlocks() as ArrayList<Block>

            if (node.elementType === KtNodeTypes.DOT_QUALIFIED_EXPRESSION || node.elementType === KtNodeTypes.SAFE_ACCESS_EXPRESSION) {
                val operationBlockIndex = findNodeBlockIndex(nodeSubBlocks, QUALIFIED_OPERATION)
                if (operationBlockIndex != -1) {
                    // Create fake ".something" or "?.something" block here, so child indentation will be
                    // relative to it when it starts from new line (see Indent javadoc).

                    val operationBlock = nodeSubBlocks[operationBlockIndex]
                    val operationSyntheticBlock = SyntheticKotlinBlock(
                            (operationBlock as ASTBlock).node,
                            nodeSubBlocks.subList(operationBlockIndex, nodeSubBlocks.size),
                            null, operationBlock.getIndent(), null, spacingBuilder) { createSyntheticSpacingNodeBlock(it) }

                    nodeSubBlocks = ContainerUtil.addAll(
                            ContainerUtil.newArrayList(nodeSubBlocks.subList(0, operationBlockIndex)),
                            operationSyntheticBlock)
                }
            }

            mySubBlocks = nodeSubBlocks
        }
        return mySubBlocks!!
    }

    protected fun createChildIndent(child: ASTNode): Indent? {
        val childParent = child.treeParent
        val childType = child.elementType

        if (childParent != null && childParent.treeParent != null) {
            if (childParent.elementType === KtNodeTypes.BLOCK && childParent.treeParent.elementType === KtNodeTypes.SCRIPT) {
                return Indent.getNoneIndent()
            }
        }

        // do not indent child after heading comments inside declaration
        if (childParent != null && childParent.psi is KtDeclaration) {
            val prev = getPrevWithoutWhitespace(child)
            if (prev != null && prev.elementType in KtTokens.COMMENTS && getPrevWithoutWhitespaceAndComments(prev) == null) {
                return Indent.getNoneIndent()
            }
        }

        for (strategy in INDENT_RULES) {
            val indent = strategy.getIndent(child)
            if (indent != null) {
                return indent
            }
        }

        // TODO: Try to rewrite other rules to declarative style
        if (childParent != null) {
            val parentType = childParent.elementType

            if (parentType === KtNodeTypes.VALUE_PARAMETER_LIST || parentType === KtNodeTypes.VALUE_ARGUMENT_LIST) {
                val prev = getPrevWithoutWhitespace(child)
                if (childType === KtTokens.RPAR && (prev == null || prev.elementType !== TokenType.ERROR_ELEMENT)) {
                    return Indent.getNoneIndent()
                }

                return Indent.getContinuationWithoutFirstIndent()
            }

            if (parentType === KtNodeTypes.TYPE_PARAMETER_LIST || parentType === KtNodeTypes.TYPE_ARGUMENT_LIST) {
                return Indent.getContinuationWithoutFirstIndent()
            }
        }

        return Indent.getNoneIndent()
    }

    private fun buildSubBlocks(): List<Block> {
        val blocks = ArrayList<Block>()

        val childrenAlignmentStrategy = getChildrenAlignmentStrategy()
        val wrappingStrategy = getWrappingStrategy()

        var child: ASTNode? = node.firstChildNode
        while (child != null) {
            val childType = child.elementType

            if (child.textRange.length == 0) {
                child = child.treeNext
                continue
            }

            if (childType === TokenType.WHITE_SPACE) {
                child = child.treeNext
                continue
            }

            blocks.add(buildSubBlock(child, childrenAlignmentStrategy, wrappingStrategy))
            child = child.treeNext
        }

        return blocks
    }

    private fun getWrappingStrategy(): WrappingStrategy {
        val commonSettings = settings.getCommonSettings(KotlinLanguage.INSTANCE)
        val elementType = node.elementType

        if (elementType === KtNodeTypes.VALUE_ARGUMENT_LIST) {
            return getWrappingStrategyForItemList(commonSettings.CALL_PARAMETERS_WRAP, KtNodeTypes.VALUE_ARGUMENT)
        }
        if (elementType === KtNodeTypes.VALUE_PARAMETER_LIST) {
            val parentElementType = node.treeParent.elementType
            if (parentElementType === KtNodeTypes.FUN || parentElementType === KtNodeTypes.CLASS) {
                return getWrappingStrategyForItemList(commonSettings.METHOD_PARAMETERS_WRAP, KtNodeTypes.VALUE_PARAMETER)
            }
        }

        return WrappingStrategy.NoWrapping
    }
}

private val INDENT_RULES = arrayOf<NodeIndentStrategy>(
        strategy("No indent for braces in blocks")
                .`in`(KtNodeTypes.BLOCK, KtNodeTypes.CLASS_BODY, KtNodeTypes.FUNCTION_LITERAL)
                .forType(KtTokens.RBRACE, KtTokens.LBRACE)
                .set(Indent.getNoneIndent()),

        strategy("Indent for block content")
                .`in`(KtNodeTypes.BLOCK, KtNodeTypes.CLASS_BODY, KtNodeTypes.FUNCTION_LITERAL)
                .notForType(KtTokens.RBRACE, KtTokens.LBRACE, KtNodeTypes.BLOCK)
                .set(Indent.getNormalIndent(false)),

        strategy("Indent for property accessors")
                .`in`(KtNodeTypes.PROPERTY).forType(KtNodeTypes.PROPERTY_ACCESSOR)
                .set(Indent.getNormalIndent()),

        strategy("For a single statement in 'for'")
                .`in`(KtNodeTypes.BODY).notForType(KtNodeTypes.BLOCK)
                .set(Indent.getNormalIndent()),

        strategy("For the entry in when")
                .forType(KtNodeTypes.WHEN_ENTRY)
                .set(Indent.getNormalIndent()),

        strategy("For single statement in THEN and ELSE")
                .`in`(KtNodeTypes.THEN, KtNodeTypes.ELSE).notForType(KtNodeTypes.BLOCK)
                .set(Indent.getNormalIndent()),

        strategy("Indent for parts")
                .`in`(KtNodeTypes.PROPERTY, KtNodeTypes.FUN, KtNodeTypes.DESTRUCTURING_DECLARATION)
                .notForType(KtNodeTypes.BLOCK, KtTokens.FUN_KEYWORD, KtTokens.VAL_KEYWORD, KtTokens.VAR_KEYWORD)
                .set(Indent.getContinuationWithoutFirstIndent()),

        strategy("Chained calls")
                .`in`(KtNodeTypes.DOT_QUALIFIED_EXPRESSION, KtNodeTypes.SAFE_ACCESS_EXPRESSION)
                .set(Indent.getContinuationWithoutFirstIndent(false)),

        strategy("Delegation list")
                .`in`(KtNodeTypes.SUPER_TYPE_LIST, KtNodeTypes.INITIALIZER_LIST)
                .set(Indent.getContinuationIndent(false)),

        strategy("Indices")
                .`in`(KtNodeTypes.INDICES)
                .set(Indent.getContinuationIndent(false)),

        strategy("Binary expressions")
                .`in`(BINARY_EXPRESSIONS)
                .set(Indent.getContinuationWithoutFirstIndent(false)),

        strategy("Parenthesized expression")
                .`in`(KtNodeTypes.PARENTHESIZED)
                .set(Indent.getContinuationWithoutFirstIndent(false)),

        strategy("KDoc comment indent")
                .`in`(KDOC_CONTENT)
                .forType(KDocTokens.LEADING_ASTERISK, KDocTokens.END)
                .set(Indent.getSpaceIndent(KDOC_COMMENT_INDENT)),

        strategy("Block in when entry")
                .`in`(KtNodeTypes.WHEN_ENTRY)
                .notForType(KtNodeTypes.BLOCK, KtNodeTypes.WHEN_CONDITION_EXPRESSION, KtNodeTypes.WHEN_CONDITION_IN_RANGE, KtNodeTypes.WHEN_CONDITION_IS_PATTERN, KtTokens.ELSE_KEYWORD, KtTokens.ARROW)
                .set(Indent.getNormalIndent()))

private fun getPrevWithoutWhitespace(pNode: ASTNode?): ASTNode? {
    var node = pNode
    node = node!!.treePrev
    while (node != null && node.elementType === TokenType.WHITE_SPACE) {
        node = node.treePrev
    }

    return node
}

private fun getPrevWithoutWhitespaceAndComments(pNode: ASTNode?): ASTNode? {
    var node = pNode
    node = node!!.treePrev
    while (node != null && (node.elementType === TokenType.WHITE_SPACE || KtTokens.COMMENTS.contains(node.elementType))) {
        node = node.treePrev
    }

    return node
}

private fun getWrappingStrategyForItemList(wrapType: Int, itemType: IElementType): WrappingStrategy {
    val itemWrap = Wrap.createWrap(wrapType, false)
    return object : WrappingStrategy {
        override fun getWrap(childElementType: IElementType): Wrap? {
            return if (childElementType === itemType) itemWrap else null
        }
    }
}

private fun findNodeBlockIndex(blocks: List<Block>, tokenSet: TokenSet): Int {
    return blocks.indexOfFirst { block ->
        if (block !is ASTBlock) return@indexOfFirst false

        val node = block.node
        node != null && node.elementType in tokenSet
    }
}