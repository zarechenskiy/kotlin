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

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.asm4.*;
import org.jetbrains.asm4.commons.Method;
import org.jetbrains.asm4.tree.*;
import org.jetbrains.jet.OutputFile;
import org.jetbrains.jet.codegen.*;
import org.jetbrains.jet.codegen.context.MethodContext;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetFunctionLiteralExpression;
import org.jetbrains.jet.lang.resolve.BindingContextUtils;
import org.jetbrains.jet.lang.resolve.name.FqName;

import java.io.IOException;
import java.util.*;

import static org.jetbrains.asm4.Opcodes.V1_6;

public class LambdaTransformer extends InlineTransformer {

    private final MethodNode constructor;

    private final MethodNode invoke;

    private final InliningInfo info;

    private final Map<String, Integer> paramMapping = new HashMap<String, Integer>();

    private final Type oldLambdaType;

    private final Type newLambdaType;

    private int classAccess;
    private String signature;
    private String superName;
    private String[] interfaces;

    public LambdaTransformer(String lambdaInternalName, InliningInfo info) {
        super(info.state);
        this.info = info;
        this.oldLambdaType = Type.getObjectType(lambdaInternalName);
        newLambdaType = Type.getObjectType(info.nameGenerator.genLambdaClassName());

        //try to find just compiled classes then in dependencies
        ClassReader reader;
        try {
            OutputFile outputFile = state.getFactory().get(lambdaInternalName + ".class");
            if (outputFile != null) {
                reader = new ClassReader(outputFile.asByteArray());
            } else {
                VirtualFile file = InlineCodegenUtil.findVirtualFile(state.getProject(), new FqName(lambdaInternalName), false);
                if (file == null) {
                    throw new RuntimeException("Couldn't find virtual file for " + lambdaInternalName);
                }
                reader = new ClassReader(file.getInputStream());
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        constructor = getMethodNode(reader, "<init>");
        invoke = getMethodNode(reader, "invoke");
    }

    private void buildInvokeParams(ParametersBuilder builder) {
        builder.addThis(oldLambdaType, false);

        Type[] types = Type.getArgumentTypes(invoke.desc);
        for (Type type : types) {
            builder.addNextParameter(type, false, null);
        }
    }

    public void doTransform(ConstructorInvocation invocation) {
        ClassBuilder classBuilder = createClassBuilder();

        classBuilder.defineClass(null,
                                 V1_6,
                                 classAccess,
                                 newLambdaType.getInternalName(),
                                 signature,
                                 superName,
                                 interfaces
        );
        ParametersBuilder builder = ParametersBuilder.newBuilder();
        Parameters parameters = getLambdaParameters(builder, invocation);

        MethodVisitor invokeVisitor = newMethod(classBuilder, invoke);
        MethodInliner inliner = new MethodInliner(invoke, parameters, info.subInline(info.nameGenerator.subGenerator("lambda")), oldLambdaType,
                                                  new LambdaFieldRemapper());
        inliner.doTransformAndMerge(invokeVisitor, new VarRemapper.ParamRemapper(parameters, null), new InlineFieldRemapper(oldLambdaType.getInternalName(), newLambdaType.getInternalName()), false);
        invokeVisitor.visitMaxs(-1, -1);

        generateConstructorAndFields(classBuilder, builder, invocation);

        classBuilder.done();

        invocation.setNewLambdaType(newLambdaType);
    }

    private void generateConstructorAndFields(@NotNull ClassBuilder classBuilder, @NotNull ParametersBuilder builder, @NotNull ConstructorInvocation invocation) {
        List<CapturedParamInfo> infos = builder.buildCaptured();
        List<Pair<String, Type>> newConstructorSignature = new ArrayList<Pair<String, Type>>();
        for (CapturedParamInfo capturedParamInfo : infos) {
            if (capturedParamInfo.getLambda() == null) { //not inlined
                newConstructorSignature.add(new Pair<String, Type>(capturedParamInfo.getFieldName(), capturedParamInfo.getType()));
            }
        }

        List<Pair<String, Type>> captured = newConstructorSignature;
        List<FieldInfo> fields = AsmUtil.transformCapturedParams(newConstructorSignature, newLambdaType);

        AsmUtil.genClosureFields(captured, classBuilder);

        Method newConstructor = ClosureCodegen.generateConstructor(classBuilder, fields, null, Type.getObjectType(superName), state);
        invocation.setNewConstructorDescriptor(newConstructor.getDescriptor());
    }

    private Parameters getLambdaParameters(ParametersBuilder builder, ConstructorInvocation invocation) {
        buildInvokeParams(builder);
        extractParametersMapping(constructor, builder, invocation);
        return builder.buildParameters();
    }

    private ClassBuilder createClassBuilder() {
        PsiElement element = BindingContextUtils.descriptorToDeclaration(state.getBindingContext(), info.startFunction);
        assert element != null : "Couldn't find declaration for " + info.startFunction;

        return state.getFactory().forLambdaInlining(newLambdaType, element.getContainingFile());
    }

    private MethodVisitor newMethod(ClassBuilder builder, MethodNode original) {
        MethodVisitor visitor = builder.newMethod(
                null,
                original.access,
                original.name,
                original.desc,
                original.signature,
                null //TODO: change signature to list
        );

        return visitor;
    }

    private void extractParametersMapping(MethodNode constructor, ParametersBuilder builder, ConstructorInvocation invocation) {
        Map<Integer, InlinableAccess> indexToLambda = invocation.getAccess();

        AbstractInsnNode cur = constructor.instructions.getFirst();
        cur = cur.getNext(); //skip super call
        List<LambdaInfo> additionalCaptured = new ArrayList<LambdaInfo>(); //captured var of inlined parameter
        while (cur != null) {
            if (cur.getType() == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode fieldNode = (FieldInsnNode) cur;
                VarInsnNode previous = (VarInsnNode) fieldNode.getPrevious();
                int varIndex = previous.var;
                paramMapping.put(fieldNode.name, varIndex);
                System.out.println(fieldNode.name + " "  + varIndex);

                CapturedParamInfo info = builder.addCapturedParam(fieldNode.name, Type.getType(fieldNode.desc), false, null);
                InlinableAccess access = indexToLambda.get(varIndex);
                if (access != null) {
                    LambdaInfo accessInfo = access.getInfo();
                    if (accessInfo != null) {
                        info.setLambda(accessInfo);
                        additionalCaptured.add(accessInfo);
                    }
                }
            }
            cur = cur.getNext();
        }

        ArrayList recaptured = new ArrayList();
        for (LambdaInfo info : additionalCaptured) {
            List<CapturedParamInfo> vars = info.getCapturedVars();
            for (CapturedParamInfo var : vars) {
                CapturedParamInfo recapturedParamInfo = builder.addCapturedParam(getNewFieldName(var.getFieldName()), var.getType(), true, var);
                recaptured.add(var);
            }
        }

        invocation.setRecaptured(recaptured);
    }

    public MethodNode getMethodNode(ClassReader reader, final String methodName) {
        final MethodNode[] methodNode = new MethodNode[1];
        reader.accept(new ClassVisitor(InlineCodegenUtil.API) {

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                LambdaTransformer.this.classAccess = access;
                LambdaTransformer.this.signature = signature;
                LambdaTransformer.this.superName = superName;
                LambdaTransformer.this.interfaces = interfaces;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (methodName.equals(name)) {
                    assert methodNode[0] == null;
                    return methodNode[0] = new MethodNode(access, name, desc, signature, exceptions);
                }
                return null;
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        if (methodNode[0] == null) {
            throw new RuntimeException("Couldn't find '" + methodName + "' method of lambda class " + oldLambdaType.getInternalName());
        }

        return methodNode[0];
    }

    public void calculatedDataForTransformation() {

    }

    @Override
    public void inlineCall(CallableMethod callableMethod, ClassVisitor visitor) {

    }

    @Override
    public void putInLocal(
            Type type, StackValue stackValue, ValueParameterDescriptor valueParameterDescriptor
    ) {

    }

    @Override
    public boolean shouldPutValue(
            Type type, StackValue stackValue, MethodContext context, ValueParameterDescriptor descriptor
    ) {
        assert false;
        return false;
    }

    @Override
    public void putHiddenParams() {
        assert false;
    }

    @Override
    public void leaveTemps() {
        assert false;
    }

    @Override
    public boolean isInliningClosure(
            JetExpression expression, ValueParameterDescriptor valueParameterDescriptora
    ) {
        assert false;
        return false;
    }

    @Override
    public void rememberClosure(JetFunctionLiteralExpression expression, Type type) {

    }

    @Override
    public void putCapturedInLocal(
            Type type, StackValue stackValue, ValueParameterDescriptor valueParameterDescriptor, int index
    ) {

    }

    public Type getNewLambdaType() {
        return newLambdaType;
    }

    public static String getNewFieldName(String oldName) {
        return oldName + "$inlined";
    }
}
