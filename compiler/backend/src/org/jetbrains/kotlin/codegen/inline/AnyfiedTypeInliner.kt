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
import org.jetbrains.kotlin.codegen.generateALoad
import org.jetbrains.kotlin.codegen.generateAReturn
import org.jetbrains.kotlin.codegen.generateLoad
import org.jetbrains.kotlin.codegen.generateStore
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.codegen.optimization.common.intConstant
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*

class AnyfiedTypeInliner(private val parametersMapping: TypeParameterMappings?) {
    enum class OperationKind {
        GET, ALOAD, ASTORE, LOCALVARIABLE, AALOAD, ARETURN;

        val id: Int get() = ordinal
    }

    companion object {
        @JvmField val ANYFIED_OPERATION_MARKER_METHOD_NAME = "anyfiedOperationMarker"

        private fun isOperationAnyfiedMarker(insn: AbstractInsnNode) = isAnyfiedMarker(insn) { it == ANYFIED_OPERATION_MARKER_METHOD_NAME }

        private fun isAnyfiedMarker(insn: AbstractInsnNode, namePredicate: (String) -> Boolean): Boolean {
            if (insn.opcode != Opcodes.INVOKESTATIC || insn !is MethodInsnNode) return false
            return insn.owner == IntrinsicMethods.INTRINSICS_CLASS_NAME && namePredicate(insn.name)
        }
    }

    private val hasAnyfiedParameters = parametersMapping?.hasAnyfiedParameters() ?: false

    private var maxStackSize = 0

    fun reifyInstructions(node: MethodNode) {
        if (!hasAnyfiedParameters) return

        val instructions = node.instructions
        maxStackSize = 0

        for (insn in instructions.toArray()) {
            if (isOperationAnyfiedMarker(insn)) {
                processAnyfiedMarker(insn as MethodInsnNode, instructions)
            }
        }

        node.maxStack = node.maxStack + maxStackSize
    }

    fun processAnyfiedMarker(insn: MethodInsnNode, instructions: InsnList) {
        val operationKind = insn.anyfiedOperationKind ?: return
        val reificationArgument = insn.reificationArgument ?: return
        val mapping = parametersMapping?.get(reificationArgument.parameterName) ?: return

        if (mapping.asmType != null) {
            val (asmType, kotlinType) = reificationArgument.reify(mapping.asmType, mapping.type)

            val isValueType = KotlinBuiltIns.isPrimitiveValueType(kotlinType)

            val specialized = isValueType && when (operationKind) {
                OperationKind.ALOAD -> processLocalVariableLoad(insn, instructions, asmType, kotlinType)
                OperationKind.ASTORE -> processLocalVariableStore(insn, instructions, asmType, kotlinType)
                OperationKind.AALOAD -> processLoadValueTypeFromArray(insn, instructions, asmType, kotlinType)
                OperationKind.ARETURN -> processReturn(insn, instructions, asmType, kotlinType)
                else -> false
            }

            if (specialized || !isValueType) {
                instructions.remove(insn.previous.previous!!) // PUSH operation ID
                instructions.remove(insn.previous!!) // PUSH type parameter
                instructions.remove(insn) // INVOKESTATIC marker method
            }
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

    inline private fun rewriteNextTypeInsn(
            marker: MethodInsnNode,
            expectedNextOpcode: Int,
            rewrite: (AbstractInsnNode) -> Boolean
    ): Boolean {
        val next = marker.next ?: return false
        if (next.opcode != expectedNextOpcode) return false
        return rewrite(next)
    }
}

class AnyfiedLocalVarMapping {
    val localVarMapping: MutableMap<String, String> = hashMapOf()

    fun addMapping(variableName: String, anyfiedSignature: String) {
        localVarMapping.put(variableName, anyfiedSignature)
    }

    fun getMapping(variableName: String): String? = localVarMapping[variableName]

    fun hasMapping(variableName: String): Boolean = variableName in localVarMapping
}

private val MethodInsnNode.anyfiedOperationKind: AnyfiedTypeInliner.OperationKind?
    get() = previous?.previous?.intConstant?.let {
        AnyfiedTypeInliner.OperationKind.values().getOrNull(it)
    }
