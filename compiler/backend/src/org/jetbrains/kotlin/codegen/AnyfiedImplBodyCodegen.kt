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

package org.jetbrains.kotlin.codegen

import com.intellij.util.ArrayUtil
import org.jetbrains.kotlin.codegen.context.ClassContext
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.impl.ClassDescriptorImpl
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import java.util.*

class AnyfiedImplBodyCodegen(
        aClass: KtClassOrObject,
        context: ClassContext,
        v: ClassBuilder,
        state: GenerationState,
        parentCodegen: MemberCodegen<*>?
) : ClassBodyCodegen(aClass, context, v, state, parentCodegen) {

    override fun classForInnerClassRecord(): ClassDescriptor? {
        if (DescriptorUtils.isLocal(descriptor)) return null
        val classDescriptorImpl = ClassDescriptorImpl(
                descriptor, Name.identifier(JvmAbi.ANYFIED_IMPLS_CLASS_NAME),
                Modality.FINAL, ClassKind.CLASS, Collections.emptyList(), SourceElement.NO_SOURCE)

        classDescriptorImpl.initialize(MemberScope.Empty, emptySet(), null)
        return classDescriptorImpl
    }


    override fun generateKotlinMetadataAnnotation() {
        writeSyntheticClassMetadata(v)
    }

    override fun generateDeclaration() {
        v.defineClass(
                myClass, state.classFileVersion, Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL or Opcodes.ACC_SUPER,
                typeMapper.mapAnyfiedImpls(descriptor).internalName,
                null, "java/lang/Object", ArrayUtil.EMPTY_STRING_ARRAY
        )
        v.visitSource(myClass.containingFile.name, null)
    }
}