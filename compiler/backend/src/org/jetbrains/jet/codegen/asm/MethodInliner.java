package org.jetbrains.jet.codegen.asm;

import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.asm4.Label;
import org.jetbrains.asm4.Opcodes;
import org.jetbrains.asm4.Type;
import org.jetbrains.asm4.commons.InstructionAdapter;
import org.jetbrains.asm4.commons.Method;
import org.jetbrains.asm4.tree.*;
import org.jetbrains.asm4.tree.analysis.*;
import org.jetbrains.jet.codegen.ClosureCodegen;
import org.jetbrains.jet.codegen.StackValue;
import org.jetbrains.jet.codegen.state.JetTypeMapper;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;
import org.jetbrains.jet.lang.types.JetType;

import java.util.*;

import static org.jetbrains.jet.codegen.asm.InlineCodegenUtil.isFunctionConstructorCall;
import static org.jetbrains.jet.codegen.asm.InlineCodegenUtil.isInvokeOnInlinable;
import static org.jetbrains.jet.codegen.asm.InlineTransformer.putStackValuesIntoLocals;

public class MethodInliner {

    private final MethodNode node;

    private final Parameters parameters;

    private final InliningInfo parent;

    @Nullable
    private LambdaInfo capturedInfo;

    private final JetTypeMapper typeMapper;

    private final List<InlinableAccess> inlinableInvocation = new ArrayList<InlinableAccess>();

    private final List<ConstructorInvocation> constructorInvocation = new ArrayList<ConstructorInvocation>();

    public MethodInliner(@NotNull MethodNode node, Parameters parameters, @NotNull InliningInfo parent, @Nullable LambdaInfo capturedInfo) {
        this.node = node;
        this.parameters = parameters;
        this.parent = parent;
        this.capturedInfo = capturedInfo;
        this.typeMapper = parent.state.getTypeMapper();
    }

    public void doTransformAndMerge(InstructionAdapter adapter, VarRemapper.ParamRemapper remapper) {
        //analyze body
        MethodNode transformedNode = node;
        try {
            transformedNode = markPlacesForInlineAndRemoveInlinable(transformedNode);
        }
        catch (AnalyzerException e) {
            throw new RuntimeException(e);
        }

        transformedNode = doInline(transformedNode);
        removeClosureAssertions(transformedNode);
        transformedNode.instructions.resetLabels();

        Label end = new Label();
        RemapVisitor visitor = new RemapVisitor(adapter, end, remapper);
        transformedNode.accept(visitor);
        visitor.visitLabel(end);

    }

    private MethodNode doInline(MethodNode node) {

        final LinkedList<InlinableAccess> infos = new LinkedList<InlinableAccess>(inlinableInvocation);

        MethodNode resultNode = new MethodNode(node.access, node.name, node.desc, node.signature, null);

        //TODO add reset to counter
        CounterAdapter inliner = new CounterAdapter(resultNode, parameters.totalSize()) {

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                if (/*INLINE_RUNTIME.equals(owner) &&*/ isInvokeOnInlinable(owner, name)) { //TODO add method
                    assert !infos.isEmpty();
                    InlinableAccess inlinableAccess = infos.remove();

                    if (!inlinableAccess.isInlinable()) {
                        //noninlinable closure
                        super.visitMethodInsn(opcode, owner, name, desc);
                        return;
                    }
                    LambdaInfo info = getLambda(inlinableAccess.index);

                    int valueParamShift = getNextLocalIndex();

                    putStackValuesIntoLocals(info.getParamsWithoutCapturedValOrVar(), valueParamShift, this, desc);

                    List<ParameterInfo> lambdaParameters = inlinableAccess.getParameters();

                    Parameters params = new Parameters(lambdaParameters, Parameters.transformList(info.getCapturedVars(), lambdaParameters.size()));

                    MethodInliner inliner = new MethodInliner(info.getNode(), params, new InliningInfo(null, null, null, null, parent.state), info);

                    VarRemapper.ParamRemapper remapper = new VarRemapper.ParamRemapper(lambdaParameters.size(), 0, params, new VarRemapper.ShiftRemapper(valueParamShift, null));
                    inliner.doTransformAndMerge(new ShiftAdapter(this.mv, 0), remapper); //TODO add skipped this and receiver

                    Method bridge = typeMapper.mapSignature(ClosureCodegen.getInvokeFunction(info.getFunctionDescriptor())).getAsmMethod();
                    Method delegate = typeMapper.mapSignature(info.getFunctionDescriptor()).getAsmMethod();
                    StackValue.onStack(delegate.getReturnType()).put(bridge.getReturnType(), this);
                }
                //else if (inlineClosures && isFunctionConstructorCall(owner, name)) { //TODO add method
                //    ConstructorInvocation invocation = constructorInvocation.remove(0);
                //    super.visitMethodInsn(opcode, owner, name, desc);
                //}
                else {
                    super.visitMethodInsn(opcode, owner, name, desc);
                }
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (capturedInfo != null && capturedInfo.getLambdaClassType().getInternalName().equals(owner)) {
                    //todo check only inlinable

                    Collection<CapturedParamInfo> vars = capturedInfo.getCapturedVars();
                    CapturedParamInfo result = null;
                    for (CapturedParamInfo valueDescriptor : vars) {
                        if (valueDescriptor.getFieldName().equals(name)) {
                            result = valueDescriptor;
                            break;
                        }
                    }
                    if (result == null) {
                        throw new UnsupportedOperationException("Coudn't find field " +
                                                                owner +
                                                                "." +
                                                                name +
                                                                " (" +
                                                                desc +
                                                                ") in captured vars of " +
                                                                capturedInfo.getFunctionLiteral().getText());
                    }

                    opcode = opcode == Opcodes.GETFIELD ? result.getType().getOpcode(Opcodes.ILOAD) : result.getType().getOpcode(
                            Opcodes.ISTORE);

                    visitVarInsn(opcode, -(result.getRemapIndex() + 1)); //to be shure it negative
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
            }
        };

        node.accept(inliner);

        return resultNode;
    }

    public void merge() {

    }

    @NotNull
    public MethodNode prepareNode(@NotNull MethodNode node) {
        if (parameters.getCaptured().size() != 0) {
            final int capturedParamsSize = parameters.getCaptured().size();
            final int realParametersSize = parameters.getReal().size();
            Type[] types = Type.getArgumentTypes(node.desc);
            Type returnType = Type.getReturnType(node.desc);

            ArrayList<Type> capturedTypes = parameters.getCapturedTypes();
            Type[] allTypes = ArrayUtil.mergeArrays(types, capturedTypes.toArray(new Type[capturedTypes.size()]));

            MethodNode transformedNode = new MethodNode(node.access, node.name, Type.getMethodDescriptor(returnType, allTypes), node.signature, null) {

                @Override
                public void visitVarInsn(int opcode, int var) {
                    int newIndex;
                    if (var < realParametersSize) {
                        newIndex = var;
                    } else {
                        newIndex = var + capturedParamsSize;
                    }
                    super.visitVarInsn(opcode, newIndex);
                }

                @Override
                public void visitIincInsn(int var, int increment) {
                    int newIndex;
                    if (var < realParametersSize) {
                        newIndex = var;
                    } else {
                        newIndex = var + capturedParamsSize;
                    }
                    super.visitIincInsn(newIndex, increment);
                }
            };
            node.accept(transformedNode);
            transformedNode.visitMaxs(30, 30);

            if (capturedInfo != null) {
                transformCaptured(node, parameters, capturedInfo);
            }
            return transformedNode;
        }

        return node;
    }

    protected MethodNode markPlacesForInlineAndRemoveInlinable(@NotNull MethodNode node) throws AnalyzerException {
        node = prepareNode(node);

        Analyzer<SourceValue> analyzer = new Analyzer<SourceValue>(new SourceInterpreter());
        Frame<SourceValue>[] sources = analyzer.analyze("fake", node);

        AbstractInsnNode cur = node.instructions.getFirst();
        int index = 0;
        while (cur != null) {
            if (cur.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) cur;
                String owner = methodInsnNode.owner;
                String desc = methodInsnNode.desc;
                String name = methodInsnNode.name;
                //TODO check closure
                int paramLength = Type.getArgumentTypes(desc).length + 1;//non static
                if (isInvokeOnInlinable(owner, name) /*&& methodInsnNode.owner.equals(INLINE_RUNTIME)*/) {
                    Frame<SourceValue> frame = sources[index];
                    SourceValue sourceValue = frame.getStack(frame.getStackSize() - paramLength);
                    assert sourceValue.insns.size() == 1;

                    AbstractInsnNode insnNode = sourceValue.insns.iterator().next();
                    assert insnNode.getType() == AbstractInsnNode.VAR_INSN && insnNode.getOpcode() == Opcodes.ALOAD : insnNode.toString();

                    int varIndex = ((VarInsnNode) insnNode).var;
                    LambdaInfo lambdaInfo =  getLambda(varIndex);
                    boolean isInlinable = lambdaInfo != null;
                    if (isInlinable) { //TODO: maybe add separate map for noninlinable inlinableInvocation
                        //remove inlinable access
                        node.instructions.remove(insnNode);
                    }

                    inlinableInvocation.add(new InlinableAccess(varIndex, isInlinable, getParametersInfo(lambdaInfo, desc)));
                }
                else if (isFunctionConstructorCall(owner, name)) {
                    Frame<SourceValue> frame = sources[index];
                    ArrayList<InlinableAccess> infos = new ArrayList<InlinableAccess>();
                    int paramStart = frame.getStackSize() - paramLength;

                    for (int i = 0; i < paramLength; i++) {
                        SourceValue sourceValue = frame.getStack(paramStart + i);
                        if (sourceValue.insns.size() == 1) {
                            AbstractInsnNode insnNode = sourceValue.insns.iterator().next();
                            if (insnNode.getType() == AbstractInsnNode.VAR_INSN && insnNode.getOpcode() == Opcodes.ALOAD) {
                                int varIndex = ((VarInsnNode) insnNode).var;
                                LambdaInfo lambdaInfo = getLambda(varIndex);
                                if (lambdaInfo != null) {
                                    InlinableAccess inlinableAccess = new InlinableAccess(varIndex, true, null);
                                    inlinableAccess.setInfo(lambdaInfo);
                                    infos.add(inlinableAccess);

                                    //remove inlinable access
                                    node.instructions.remove(insnNode);
                                }
                            }
                        }
                    }

                    ConstructorInvocation invocation = new ConstructorInvocation(owner, infos);
                    constructorInvocation.add(invocation);
                }
            }
            cur = cur.getNext();
            index++;
        }
        return node;
    }

    public List<ParameterInfo> getParametersInfo(LambdaInfo info, String desc) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        Type[] types = Type.getArgumentTypes(desc);

        //add skipped this cause closure doesn't have it
        result.add(ParameterInfo.STUB);
        int index = 1;

        if (info != null) {
            List<ValueParameterDescriptor> valueParameters = info.getFunctionDescriptor().getValueParameters();
            for (ValueParameterDescriptor parameter : valueParameters) {
                Type type = typeMapper.mapType(parameter.getType());
                int paramIndex = index++;
                result.add(new ParameterInfo(type, false, -1, paramIndex));
                if (type.getSize() == 2) {
                    result.add(ParameterInfo.STUB);
                }
            }
        } else {
            for (int i = 0; i < types.length; i++) {
                Type type = types[i];
                int paramIndex = index++;
                result.add(new ParameterInfo(type, false, -1, paramIndex));
                if (type.getSize() == 2) {
                    result.add(ParameterInfo.STUB);
                }
            }
        }
        return result;
    }

    public LambdaInfo getLambda(int index) {
        if (index < parameters.totalSize()) {
            return parameters.get(index).getLambda();
        }
        return null;
    }

    private void removeClosureAssertions(MethodNode node) {
        AbstractInsnNode cur = node.instructions.getFirst();
        while (cur != null && cur.getNext() != null) {
            AbstractInsnNode next = cur.getNext();
            if (next.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                if (methodInsnNode.name.equals("checkParameterIsNotNull") && methodInsnNode.owner.equals("jet/runtime/Intrinsics")) {
                    AbstractInsnNode prev = cur.getPrevious();
                    assert prev.getType() == AbstractInsnNode.VAR_INSN && prev.getOpcode() == Opcodes.ALOAD;
                    int varIndex = ((VarInsnNode) prev).var;
                    LambdaInfo closure = getLambda(varIndex);
                    if (closure != null) {
                        node.instructions.remove(prev);
                        node.instructions.remove(cur);
                        cur = next.getNext();
                        node.instructions.remove(next);
                        next = cur;
                    }
                }
            }
            cur = next;
        }
    }

    private static MethodNode transformCaptured(@NotNull MethodNode node, @NotNull Parameters paramsToSearch, @Nullable LambdaInfo info) {
        //remove all this and shift all variables to captured ones size
        AbstractInsnNode cur = node.instructions.getFirst();
        while (cur != null) {
            if (cur.getType() == AbstractInsnNode.FIELD_INSN) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode) cur;
                //TODO check closure
                String owner = fieldInsnNode.owner;
                if (info.getLambdaClassType().getInternalName().equals(fieldInsnNode.owner)) {
                    String name = fieldInsnNode.name;
                    String desc = fieldInsnNode.desc;

                    Collection<CapturedParamInfo> vars = paramsToSearch.getCaptured();
                    CapturedParamInfo result = null;
                    for (CapturedParamInfo valueDescriptor : vars) {
                        if (valueDescriptor.getFieldName().equals(name)) {
                            result = valueDescriptor;
                            break;
                        }
                    }
                    if (result == null) {
                        throw new UnsupportedOperationException("Coudn't find field " +
                                                                owner +
                                                                "." +
                                                                name +
                                                                " (" +
                                                                desc +
                                                                ") in captured vars of " +
                                                                info.getFunctionLiteral().getText());
                    }

                    AbstractInsnNode prev = getPreviousNoLableNoLine(cur);

                    assert prev.getType() == AbstractInsnNode.VAR_INSN;
                    VarInsnNode loadThis = (VarInsnNode) prev;
                    assert /*loadThis.var == info.getCapturedVarsSize() - 1 && */loadThis.getOpcode() == Opcodes.ALOAD;

                    int opcode = fieldInsnNode.getOpcode() == Opcodes.GETFIELD ? result.getType().getOpcode(Opcodes.ILOAD) : result.getType().getOpcode(Opcodes.ISTORE);
                    VarInsnNode insn = new VarInsnNode(opcode, result.getIndex());

                    node.instructions.remove(prev); //remove aload this
                    node.instructions.insertBefore(cur, insn);
                    node.instructions.remove(cur); //remove aload this
                    cur = insn;
                }
            }
            cur = cur.getNext();
        }

        return node;
    }

    private static AbstractInsnNode getPreviousNoLableNoLine(AbstractInsnNode cur) {
        AbstractInsnNode prev = cur.getPrevious();
        while (prev.getType() == AbstractInsnNode.LABEL || prev.getType() == AbstractInsnNode.LINE) {
            prev = prev.getPrevious();
        }
        return prev;
    }
}
