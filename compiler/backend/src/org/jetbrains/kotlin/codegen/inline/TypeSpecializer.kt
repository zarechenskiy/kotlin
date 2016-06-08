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

import org.jetbrains.kotlin.codegen.context.MethodContext
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.MethodInsnNode

abstract class TypeSpecializer(val parametersMapping: TypeParameterMappings, val specializationKind: TypeSpecializationKind) {
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
}

enum class TypeSpecializationKind(
        val specializationMarkerOperation: String,
        val needClassSpecializationMarker: String) {
    REIFICATION("reifiedOperationMarker", "needClassReification") {
        override fun applicableTypeParameter(typeParameter: TypeParameterDescriptor): Boolean = typeParameter.isReified
    };
    // +ANYFICATION

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
