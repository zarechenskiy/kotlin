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

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.AsmUtil.correctElementType
import org.jetbrains.kotlin.codegen.AsmUtil.unboxType
import org.jetbrains.kotlin.codegen.Callable
import org.jetbrains.kotlin.codegen.CallableMethod
import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.org.objectweb.asm.Type

class ArrayGet : IntrinsicMethod() {
    override fun toCallable(fd: FunctionDescriptor, isSuper: Boolean, resolvedCall: ResolvedCall<*>, codegen: ExpressionCodegen): Callable =
            createIntrinsicCallable(codegen.state.typeMapper.mapToCallableMethod(fd, false)) {
                val type = correctElementType(calcReceiverType())
                val returnKotlinType = fd.returnType
                if (returnKotlinType != null && KotlinBuiltIns.isPrimitiveValueType(returnKotlinType)) {
                    it.aload(codegen.state.typeMapper.mapType(returnKotlinType))
                } else {
                    if (returnKotlinType != null && TypeUtils.isAnyfiedTypeParameter(returnKotlinType)) {
                        codegen.putAnyfiedOperationMarkerIfTypeIsReifiedParameter(returnKotlinType)
                    }
                    it.aload(type)
                }
            }
}
