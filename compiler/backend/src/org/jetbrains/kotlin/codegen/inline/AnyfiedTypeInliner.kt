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

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.optimization.common.intConstant
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*

class AnyfiedTypeParametersUsages : SpecializedTypeParametersUsages(TypeSpecializationKind.ANYFICATION)

class AnyfiedTypeInliner(parametersMapping: TypeParameterMappings?) : TypeSpecializer(parametersMapping, TypeSpecializationKind.ANYFICATION) {
    enum class OperationKind {
        GET, ALOAD, ASTORE, LOCALVARIABLE, AALOAD, ARETURN, IF_ACMP, NEW_ARRAY, COERCION, IF_ICMPLE;

        val id: Int get() = ordinal
    }

    override fun hasParametersToSpecialize(): Boolean {
        return parametersMapping?.hasAnyfiedParameters() ?: false
    }

    override fun correctNodeStack(node: MethodNode) {
    }

    override fun processInstruction(insn: MethodInsnNode, instructions: InsnList, asmType: Type, kotlinType: KotlinType): Boolean {
        val operationKind = insn.anyfiedOperationKindByNextOperation ?: return false

        val isValueType = KotlinBuiltIns.isPrimitiveValueType(kotlinType)
        if (!isValueType) return true

        return when (operationKind) {
            OperationKind.ALOAD -> processLocalVariableLoad(insn, instructions, asmType, kotlinType)
            OperationKind.ASTORE -> processLocalVariableStore(insn, instructions, asmType, kotlinType)
            OperationKind.AALOAD -> processLoadValueTypeFromArray(insn, instructions, asmType, kotlinType)
            OperationKind.ARETURN -> processReturn(insn, instructions, asmType, kotlinType)
            OperationKind.COERCION -> processCoercion(insn, instructions, asmType, kotlinType)
            OperationKind.IF_ACMP -> processAcmp(insn, instructions, asmType, kotlinType)
            OperationKind.IF_ICMPLE -> true
            else -> false
        }
    }

    private fun processReturn(
            insn: MethodInsnNode,
            instructions: InsnList,
            parameter: Type,
            kotlinType: KotlinType): Boolean {
        return rewriteNextTypeInsn(insn, Opcodes.ARETURN) { stubAALoad ->
            if (stubAALoad !is InsnNode) return false

            val newMethodNode = MethodNode(InlineCodegenUtil.API)
            generateAReturn(InstructionAdapter(newMethodNode), parameter)

            instructions.insert(insn, newMethodNode.instructions)
            instructions.remove(stubAALoad)

            return true
        }
    }

    private fun processAcmp(
            insn: MethodInsnNode,
            instructions: InsnList,
            asmType: Type,
            kotlinType: KotlinType): Boolean {

        fun process(opCode: Int, stub: AbstractInsnNode): Boolean {
            if (stub !is JumpInsnNode) return false

            if (asmType.sort == Type.INT) {
                val specializedOpcode = when (opCode) {
                    Opcodes.IF_ACMPEQ -> Opcodes.IF_ICMPEQ
                    Opcodes.IF_ACMPNE -> Opcodes.IF_ICMPNE
                    else -> return false
                }

                val newMethodNode = MethodNode(InlineCodegenUtil.API)
                generateConditionJump(InstructionAdapter(newMethodNode), asmType, stub.label.label, specializedOpcode)

                instructions.insert(insn, newMethodNode.instructions)
                instructions.remove(stub)

                return true
            }

            return false
        }

        val next = insn.next ?: return false

        return when (next.opcode) {
            Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE -> process(next.opcode, next)

            else -> false
        }
    }

    private fun processCoercion(
            insn: MethodInsnNode,
            instructions: InsnList,
            asmType: Type,
            kotlinType: KotlinType): Boolean {
        rewriteNextTypeInsn(insn, Opcodes.INSTANCEOF) { stub ->
            if (stub !is TypeInsnNode) return false

            val newMethodNode = MethodNode(InlineCodegenUtil.API)
            StackValue.coerce(asmType, AsmUtil.boxType(asmType), InstructionAdapter(newMethodNode))

            instructions.insert(insn, newMethodNode.instructions)

            return true
        }

        return true
    }

    private fun processLoadValueTypeFromArray(
            insn: MethodInsnNode,
            instructions: InsnList,
            asmType: Type,
            kotlinType: KotlinType): Boolean {
        return rewriteNextTypeInsn(insn, Opcodes.AALOAD) { stubAALoad ->
            if (stubAALoad !is InsnNode) return false

            val newMethodNode = MethodNode(InlineCodegenUtil.API)
            generateALoad(InstructionAdapter(newMethodNode), asmType)

            instructions.insert(insn, newMethodNode.instructions)
            instructions.remove(stubAALoad)

            return true
        }
    }

    private fun processLocalVariableLoad(
            insn: MethodInsnNode,
            instructions: InsnList,
            asmType: Type,
            kotlinType: KotlinType): Boolean {
        return rewriteNextTypeInsn(insn, Opcodes.ALOAD) { stubALoad ->
            if (stubALoad !is VarInsnNode) return false

            val newMethodNode = MethodNode(InlineCodegenUtil.API)
            generateLoad(InstructionAdapter(newMethodNode), asmType, stubALoad.`var`)

            instructions.insert(insn, newMethodNode.instructions)
            instructions.remove(stubALoad)

            return true
        }
    }

    private fun processLocalVariableStore(insn: MethodInsnNode,
                                          instructions: InsnList,
                                          asmType: Type,
                                          kotlinType: KotlinType): Boolean {
        return rewriteNextTypeInsn(insn, Opcodes.ASTORE) { stubALoad ->
            if (stubALoad !is VarInsnNode) return false

            val newMethodNode = MethodNode(InlineCodegenUtil.API)
            generateStore(InstructionAdapter(newMethodNode), asmType, stubALoad.`var`)

            instructions.insert(insn, newMethodNode.instructions)
            instructions.remove(stubALoad)

            return true
        }
    }
}

private val MethodInsnNode.anyfiedOperationKind: AnyfiedTypeInliner.OperationKind?
    get() = previous?.previous?.intConstant?.let {
        AnyfiedTypeInliner.OperationKind.values().getOrNull(it)
    }

private val MethodInsnNode.anyfiedOperationKindByNextOperation: AnyfiedTypeInliner.OperationKind?
    get() {
        val next = this.next ?: null
        return when (next!!.opcode) {
            Opcodes.ALOAD -> AnyfiedTypeInliner.OperationKind.ALOAD
            Opcodes.ASTORE -> AnyfiedTypeInliner.OperationKind.ASTORE
            Opcodes.AALOAD -> AnyfiedTypeInliner.OperationKind.AALOAD
            Opcodes.ARETURN -> AnyfiedTypeInliner.OperationKind.ARETURN
            Opcodes.IF_ICMPLE -> AnyfiedTypeInliner.OperationKind.IF_ICMPLE
            else -> null
        }
    }