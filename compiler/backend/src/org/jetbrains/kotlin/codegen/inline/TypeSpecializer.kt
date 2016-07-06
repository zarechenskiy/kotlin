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
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.context.MethodContext
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*

abstract class TypeSpecializer(val parametersMapping: TypeParameterMappings?, val specializationKind: TypeSpecializationKind) {
    companion object {
        fun isSpecializationOperationMarker(insn: AbstractInsnNode, specializationKind: TypeSpecializationKind) =
                isSpecializationMarker(insn) {
                    it == specializationKind.specializationMarkerOperation
                }

        private fun isSpecializationMarker(insn: AbstractInsnNode, namePredicate: (String) -> Boolean): Boolean {
            if (insn.opcode != Opcodes.INVOKESTATIC || insn !is MethodInsnNode) return false
            return insn.owner == IntrinsicMethods.INTRINSICS_CLASS_NAME && namePredicate(insn.name)
        }

        @JvmStatic
        fun isNeedClassSpecializationMarker(insn: AbstractInsnNode, specializationKind: TypeSpecializationKind): Boolean =
                isSpecializationMarker(insn) {
                    it == specializationKind.needClassSpecializationMarker
                }

        @JvmStatic
        fun putNeedClassSpecializationMarker(v: MethodVisitor, specializationKind: TypeSpecializationKind) {
            v.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    IntrinsicMethods.INTRINSICS_CLASS_NAME, specializationKind.needClassSpecializationMarker,
                    Type.getMethodDescriptor(Type.VOID_TYPE), false
            )
        }
    }

    abstract fun hasParametersToSpecialize(): Boolean

    abstract fun correctNodeStack(node: MethodNode)

    abstract fun processInstruction(insn: MethodInsnNode, instructions: InsnList, asmType: Type,
                                    kotlinType: KotlinType, removeMarkers: Boolean = true): Boolean

    /**
     * @return set of type parameters' identifiers contained in markers that should be reified further
     * e.g. when we're generating inline function containing reified T
     * and another function containing reifiable parts is inlined into that function
     */
    fun specializeInstructions(node: MethodNode, removeMarkers: Boolean = true): SpecializedTypeParametersUsages {
        if (!hasParametersToSpecialize()) return SpecializedTypeParametersUsages(specializationKind)

        val instructions = node.instructions
        val result = ReifiedTypeParametersUsages()
        for (insn in instructions.toArray()) {
            if (TypeSpecializer.isSpecializationOperationMarker(insn, specializationKind)) {
                val newName: String? = processSpecializeMarker(insn as MethodInsnNode, instructions, removeMarkers)
                if (newName != null) {
                    result.addUsedSpecializedParameter(newName)
                }
            }
        }

        correctNodeStack(node)

        return result
    }

    inline protected fun rewriteNextTypeInsn(
            marker: MethodInsnNode,
            expectedNextOpcode: Int,
            rewrite: (AbstractInsnNode) -> Boolean
    ): Boolean {
        val next = marker.next ?: return false
        if (next.opcode != expectedNextOpcode) return false
        return rewrite(next)
    }

    protected fun processNextTypeInsn(insn: MethodInsnNode, parameter: Type, expectedNextOpcode: Int): Boolean {
        if (insn.next?.opcode != expectedNextOpcode) return false
        if (AsmUtil.isPrimitive(parameter)) {
            return true
        }

        (insn.next as TypeInsnNode).desc = parameter.internalName
        return true
    }

    /**
     * @return new type parameter identifier if this marker should be reified further
     * or null if it shouldn't
     */
    private fun processSpecializeMarker(insn: MethodInsnNode, instructions: InsnList, removeMarkers: Boolean = true): String? {
        val reificationArgument = insn.reificationArgument ?: return null
        val mapping = parametersMapping?.get(reificationArgument.parameterName) ?: return null

        if (mapping.asmType != null) {
            // processInstruction method return false if marker should be reified further
            // or it's invalid (may be emitted explicitly in code)
            // they return true if instruction is reified and marker can be deleted
            val (asmType, kotlinType) = reificationArgument.reify(mapping.asmType, mapping.type)

            if (processInstruction(insn, instructions, asmType, kotlinType, removeMarkers)) {
                instructions.remove(insn.previous.previous!!) // PUSH operation ID
                instructions.remove(insn.previous!!) // PUSH type parameter
                instructions.remove(insn) // INVOKESTATIC marker method
            }

            return null
        }
        else {
            val newReificationArgument = reificationArgument.combine(mapping.reificationArgument!!)
            instructions.set(insn.previous!!, LdcInsnNode(newReificationArgument.asString()))
            return mapping.reificationArgument.parameterName
        }
    }
}

enum class TypeSpecializationKind(
        val specializationMarkerOperation: String,
        val needClassSpecializationMarker: String) {
    REIFICATION("reifiedOperationMarker", "needClassReification") {
        override fun applicableTypeParameter(typeParameter: TypeParameterDescriptor): Boolean {
            return typeParameter.isReified
        }
    },

    ANYFICATION("anyfiedOperationMarker", "needClassAnyfication") {
        override fun applicableTypeParameter(typeParameter: TypeParameterDescriptor): Boolean {
            return TypeUtils.isAnyfiedTypeParameter(typeParameter)
        }
    };

    abstract fun applicableTypeParameter(typeParameter: TypeParameterDescriptor): Boolean
}

open class SpecializedTypeParametersUsages(val specializationKind: TypeSpecializationKind) {
    val usedTypeParameters: MutableSet<String> = hashSetOf()

    fun wereUsedSpecializedParameters(): Boolean = usedTypeParameters.isNotEmpty()

    fun addUsedSpecializedParameter(name: String) {
        usedTypeParameters.add(name)
    }

    fun propagateChildUsagesWithinContext(child: SpecializedTypeParametersUsages, context: MethodContext) {
        if (!child.wereUsedSpecializedParameters()) return
        // used for propagating reified TP usages from children member codegen to parent's
        // mark enclosing object-literal/lambda as needed reification iff
        // 1. at least one of it's method contains operations to reify
        // 2. reified type parameter of these operations is not from current method signature
        // i.e. from outer scope
        child.usedTypeParameters.filterNot { name ->
            context.contextDescriptor.typeParameters.any { typeParameter ->
                specializationKind.applicableTypeParameter(typeParameter) && typeParameter.name.asString() == name
            }
        }.forEach { usedTypeParameters.add(it) }
    }

    fun mergeAll(other: SpecializedTypeParametersUsages) {
        if (!other.wereUsedSpecializedParameters()) return
        usedTypeParameters.addAll(other.usedTypeParameters)
    }
}

class TypeParameterMappings() {
    private val mappingsByName = hashMapOf<String, TypeParameterMapping>()

    fun addParameterMappingToType(name: String, type: KotlinType, asmType: Type, signature: String, isReified: Boolean, isAnyfied: Boolean) {
        mappingsByName[name] = TypeParameterMapping(
                name, type, asmType, reificationArgument = null, signature = signature, isReified = isReified, isAnyfied = isAnyfied
        )
    }

    fun addParameterMappingForFurtherReification(name: String, type: KotlinType,
                                                 reificationArgument: ReificationArgument, isReified: Boolean, isAnyfied: Boolean) {
        mappingsByName[name] = TypeParameterMapping(
                name, type, asmType = null, reificationArgument = reificationArgument,
                signature = null, isReified = isReified, isAnyfied = isAnyfied
        )
    }

    operator fun get(name: String): TypeParameterMapping? = mappingsByName[name]

    fun hasReifiedParameters() = mappingsByName.values.any { it.isReified }

    fun hasAnyfiedParameters() = mappingsByName.values.any { it.isAnyfied }

    internal inline fun forEach(l: (TypeParameterMapping) -> Unit)  {
        mappingsByName.values.forEach(l)
    }
}

class TypeParameterMapping(
        val name: String,
        val type: KotlinType,
        val asmType: Type?,
        val reificationArgument: ReificationArgument?,
        val signature: String?,
        val isReified: Boolean,
        val isAnyfied: Boolean
)

private val MethodInsnNode.reificationArgument: ReificationArgument?
    get() {
        val prev = previous!!

        val reificationArgumentRaw = when (prev.opcode) {
            Opcodes.LDC -> (prev as LdcInsnNode).cst as String
            else -> return null
        }

        val arrayDepth = reificationArgumentRaw.indexOfFirst { it != '[' }
        val parameterName = reificationArgumentRaw.substring(arrayDepth).removeSuffix("?")
        val nullable = reificationArgumentRaw.endsWith('?')

        return ReificationArgument(parameterName, nullable, arrayDepth)
    }

