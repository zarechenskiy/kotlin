/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.codegen.asm;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.asm4.MethodVisitor;
import org.jetbrains.asm4.Opcodes;
import org.jetbrains.asm4.Type;
import org.jetbrains.asm4.commons.InstructionAdapter;
import org.jetbrains.asm4.tree.MethodNode;
import org.jetbrains.jet.codegen.StackValue;
import org.jetbrains.jet.codegen.state.GenerationState;
import org.jetbrains.jet.codegen.state.JetTypeMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InlineTransformer implements Inliner {

    public final static String INVOKE = "invoke";

    protected final List<InlinableAccess> inlinableInvocation = new ArrayList<InlinableAccess>();

    protected final List<ConstructorInvocation> constructorInvocation = new ArrayList<ConstructorInvocation>();

    protected final JetTypeMapper typeMapper;

    protected final List<ParameterInfo> tempTypes = new ArrayList();

    protected final Map<Integer, LambdaInfo> expressionMap = new HashMap<Integer, LambdaInfo>();

    protected final GenerationState state;

    public InlineTransformer(@NotNull GenerationState state) {
        this.state = state;
        this.typeMapper = state.getTypeMapper();
    }

    public static void putStackValuesIntoLocals(List<Type> directOrder, int shift, InstructionAdapter mv, String descriptor) {
        Type[] actualParams = Type.getArgumentTypes(descriptor); //last param is closure itself
        assert actualParams.length == directOrder.size() : "Number of expected and actual params should be equals!";

        int size = 0;
        for (Type next : directOrder) {
            size += next.getSize();
        }

        shift += size;
        int index = directOrder.size();

        for (Type next : Lists.reverse(directOrder)) {
            shift -= next.getSize();
            Type typeOnStack = actualParams[--index];
            if (!typeOnStack.equals(next)) {
                StackValue.onStack(typeOnStack).put(next, mv);
            }
            mv.visitVarInsn(next.getOpcode(Opcodes.ISTORE), shift);
        }
    }

    protected void doInline(
            int access,
            String desc,
            MethodVisitor mv,
            MethodNode methodNode,
            int frameSize,
            final boolean inlineClosures,
            @NotNull VarRemapper remapper
    ) {

    }

    protected boolean isInvokeOnInlinable(String owner, String name) {
        return INVOKE.equals(name) && /*TODO: check type*/owner.contains("Function");
    }

    protected boolean isFunctionConstructorCall(@NotNull String internalName, @NotNull String name) {
        if (!"<init>".equals(name)) {
            return false;
        }

        return isFunctionLiteralClass(internalName);
    }

    private boolean isFunctionLiteralClass(String internalName) {
        String shortName = getLastNamePart(internalName);
        int index = shortName.lastIndexOf("$");

        if (index < 0) {
            return false;
        }

        String suffix = shortName.substring(index + 1);
        for (char c : suffix.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    @NotNull
    private String getLastNamePart(@NotNull String internalName) {
        int index = internalName.lastIndexOf("/");
        return index < 0 ? internalName : internalName.substring(index + 1);
    }

    private boolean isInitCallOfFunction(String owner, String name) {
        return "<init>".equals(name);
    }
}
