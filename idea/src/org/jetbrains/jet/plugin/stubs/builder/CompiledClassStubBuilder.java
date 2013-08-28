package org.jetbrains.jet.plugin.stubs.builder;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.descriptors.serialization.ClassData;
import org.jetbrains.jet.descriptors.serialization.Flags;
import org.jetbrains.jet.descriptors.serialization.ProtoBuf;
import org.jetbrains.jet.lang.psi.stubs.PsiJetStubWithFqName;
import org.jetbrains.jet.lang.psi.stubs.elements.JetClassElementType;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetClassBodyStubImpl;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetClassStubImpl;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetObjectStubImpl;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;
import org.jetbrains.jet.lang.resolve.java.resolver.KotlinClassFileHeader;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.List;

public class CompiledClassStubBuilder extends CompiledStubBuilderBase {

    @NotNull
    private final ProtoBuf.Class classProto;
    @NotNull
    private final FqName classFqName;

    private final StubElement parent;
    private final VirtualFile file;

    public CompiledClassStubBuilder(
            @NotNull ClassData classData,
            @NotNull FqName classFqName,
            @NotNull FqName packageFqName,
            StubElement parent,
            VirtualFile file
    ) {
        super(classData.getNameResolver(), packageFqName);
        this.parent = parent;
        this.file = file;
        this.classProto = classData.getClassProto();
        this.classFqName = classFqName;
    }

    public void createStub() {
        Name name = getNameResolver().getName(classProto.getName());
        int flags = classProto.getFlags();
        ProtoBuf.Class.Kind kind = Flags.CLASS_KIND.get(flags);
        boolean isEnumEntry = kind == ProtoBuf.Class.Kind.ENUM_ENTRY;
        //TODO: inner classes
        PsiJetStubWithFqName<?> classOrObjectStub;
        if (kind == ProtoBuf.Class.Kind.OBJECT) {
            classOrObjectStub = new PsiJetObjectStubImpl(parent, name.asString(), classFqName, true, false);
        }
        else {
            classOrObjectStub = new PsiJetClassStubImpl(JetClassElementType.getStubType(isEnumEntry), parent, classFqName.asString(), name.asString(), getSuperList(),
                                                kind == ProtoBuf.Class.Kind.TRAIT, kind == ProtoBuf.Class.Kind.ENUM_CLASS,
                                                isEnumEntry, kind == ProtoBuf.Class.Kind.ANNOTATION_CLASS, false);
        }
        PsiJetClassBodyStubImpl classBody = new PsiJetClassBodyStubImpl(classOrObjectStub);

        for (int nestedNameIndex : ContainerUtil.concat(classProto.getNestedClassNameList(), classProto.getNestedObjectNameList())) {
            Name nestedName = getNameResolver().getName(nestedNameIndex);
            VirtualFile nestedFile = findNestedClassFile(file, nestedName);
            KotlinClassFileHeader header = KotlinClassFileHeader.readKotlinHeaderFromClassFile(nestedFile); // TODO NPE®
            FqName classFqName = header.getJvmClassName().getFqName();
            // TODO package name is not needed
            new CompiledClassStubBuilder(header.readClassData(), classFqName, classFqName, classBody, nestedFile).createStub();
        }

        if (classProto.getClassObjectPresent() && kind != ProtoBuf.Class.Kind.ENUM_CLASS) { // TODO enum
            VirtualFile nestedFile = findNestedClassFile(file, Name.identifier(JvmAbi.CLASS_OBJECT_CLASS_NAME));
            KotlinClassFileHeader header = KotlinClassFileHeader.readKotlinHeaderFromClassFile(nestedFile);
            FqName classFqName = header.getJvmClassName().getFqName();
            // TODO package name is not needed
            new CompiledClassStubBuilder(header.readClassData(), classFqName, classFqName, classBody, nestedFile).createStub();
        }

        //TODO: primary constructor
        for (ProtoBuf.Callable callableProto : classProto.getMemberList()) {
            createCallableStub(classBody, callableProto);
        }
    }

    @NotNull
    private List<String> getSuperList() {
        return ContainerUtil.map(classProto.getSupertypeList(), new Function<ProtoBuf.Type, String>() {
            @Override
            public String fun(ProtoBuf.Type type) {
                assert type.getConstructor().getKind() == ProtoBuf.Type.Constructor.Kind.CLASS;
                FqName superFqName = getNameResolver().getFqName(type.getConstructor().getId());
                return superFqName.asString();
            }
        });
    }

    @Nullable
    @Override
    protected FqName getInternalFqName(@NotNull String name) {
        return null;
    }

    private VirtualFile findNestedClassFile(VirtualFile file, Name innerName) {
        String baseName = file.getNameWithoutExtension();
        VirtualFile dir = file.getParent();
        assert dir != null;

        return dir.findChild(baseName + "$" + innerName.asString() + ".class");
    }
}
