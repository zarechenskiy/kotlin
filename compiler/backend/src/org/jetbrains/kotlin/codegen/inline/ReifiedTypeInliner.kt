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

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.codegen.generateAsCast
import org.jetbrains.kotlin.codegen.generateIsCheck
import org.jetbrains.kotlin.codegen.optimization.common.intConstant
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.types.typeUtil.makeNullableIfNeeded
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*

class ReificationArgument(
        val parameterName: String, val nullable: Boolean, val arrayDepth: Int
) {
    fun asString() = "[".repeat(arrayDepth) + parameterName + (if (nullable) "?" else "")
    fun combine(replacement: ReificationArgument) =
            ReificationArgument(
                    replacement.parameterName,
                    this.nullable || (replacement.nullable && this.arrayDepth == 0),
                    this.arrayDepth + replacement.arrayDepth
            )

    fun reify(replacementAsmType: Type, kotlinType: KotlinType) =
            Pair(Type.getType("[".repeat(arrayDepth) + replacementAsmType), kotlinType.arrayOf(arrayDepth).makeNullableIfNeeded(nullable))

    private fun KotlinType.arrayOf(arrayDepth: Int): KotlinType {
        val builtins = this.builtIns
        var currentType = this

        repeat(arrayDepth) {
            currentType = builtins.getArrayType(Variance.INVARIANT, currentType)
        }

        return currentType
    }
}

class ReifiedTypeInliner(parametersMapping: TypeParameterMappings?) : TypeSpecializer(parametersMapping, TypeSpecializationKind.REIFICATION) {
    enum class OperationKind {
        NEW_ARRAY, AS, SAFE_AS, IS, JAVA_CLASS;

        val id: Int get() = ordinal
    }

    private var maxStackSize = 0

    override fun hasParametersToSpecialize(): Boolean = parametersMapping?.hasReifiedParameters() ?: false

    override fun correctNodeStack(node: MethodNode) {
        node.maxStack = node.maxStack + maxStackSize
    }

    override fun processInstruction(insn: MethodInsnNode, instructions: InsnList, asmType: Type,
                                    kotlinType: KotlinType, removeMarkers: Boolean): Boolean {
        val operationKind = insn.operationKind ?: return false
        return when (operationKind) {
            OperationKind.NEW_ARRAY -> processNewArray(insn, asmType)
            OperationKind.AS -> processAs(insn, instructions, kotlinType, asmType, safe = false)
            OperationKind.SAFE_AS -> processAs(insn, instructions, kotlinType, asmType, safe = true)
            OperationKind.IS -> processIs(insn, instructions, kotlinType, asmType)
            OperationKind.JAVA_CLASS -> processJavaClass(insn, asmType)
        }
    }

    private fun processNewArray(insn: MethodInsnNode, parameter: Type) =
            processNextTypeInsn(insn, parameter, Opcodes.ANEWARRAY)

    private fun processAs(
            insn: MethodInsnNode,
            instructions: InsnList,
            kotlinType: KotlinType,
            asmType: Type,
            safe: Boolean
    ) = rewriteNextTypeInsn(insn, Opcodes.CHECKCAST) { stubCheckcast: AbstractInsnNode ->
        if (stubCheckcast !is TypeInsnNode) return false

        val newMethodNode = MethodNode(InlineCodegenUtil.API)
        generateAsCast(InstructionAdapter(newMethodNode), kotlinType, asmType, safe)

        instructions.insert(insn, newMethodNode.instructions)
        instructions.remove(stubCheckcast)

        // TODO: refine max stack calculation (it's not always as big as +4)
        maxStackSize = Math.max(maxStackSize, 4)

        return true
    }

    private fun processIs(
            insn: MethodInsnNode,
            instructions: InsnList,
            kotlinType: KotlinType,
            asmType: Type
    ) = rewriteNextTypeInsn(insn, Opcodes.INSTANCEOF) { stubInstanceOf: AbstractInsnNode ->
        if (stubInstanceOf !is TypeInsnNode) return false

        val newMethodNode = MethodNode(InlineCodegenUtil.API)
        generateIsCheck(InstructionAdapter(newMethodNode), kotlinType, asmType)

        instructions.insert(insn, newMethodNode.instructions)
        instructions.remove(stubInstanceOf)

        // TODO: refine max stack calculation (it's not always as big as +2)
        maxStackSize = Math.max(maxStackSize, 2)
        return true
    }

    private fun processJavaClass(insn: MethodInsnNode, parameter: Type): Boolean {
        val next = insn.next
        if (next !is LdcInsnNode) return false
        next.cst = parameter
        return true
    }
}

private val MethodInsnNode.operationKind: ReifiedTypeInliner.OperationKind? get() =
    previous?.previous?.intConstant?.let {
        ReifiedTypeInliner.OperationKind.values().getOrNull(it)
    }

class ReifiedTypeParametersUsages : SpecializedTypeParametersUsages(TypeSpecializationKind.REIFICATION)
