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

package org.jetbrains.jet.plugin.caches.resolve;

import com.google.common.collect.Sets;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ClassFileViewProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.impl.compiled.DefaultClsStubBuilderFactory;
import com.intellij.psi.impl.compiled.InnerClassSourceStrategy;
import com.intellij.psi.impl.compiled.StubBuildingVisitor;
import com.intellij.psi.impl.java.stubs.impl.PsiJavaFileStubImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.PsiClassHolderFileStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.cls.ClsFormatException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.asm4.ClassReader;
import org.jetbrains.jet.asJava.*;
import org.jetbrains.jet.lang.psi.JetClassOrObject;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiUtil;
import org.jetbrains.jet.lang.resolve.java.PackageClassUtils;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.plugin.stubindex.JetAllPackagesIndex;
import org.jetbrains.jet.plugin.stubindex.JetClassByPackageIndex;
import org.jetbrains.jet.plugin.stubindex.JetFullClassNameIndex;
import org.jetbrains.jet.util.QualifiedNamesUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static org.jetbrains.jet.plugin.stubindex.JetSourceFilterScope.kotlinSourcesAndBinaries;

public class IDELightClassGenerationSupport extends LightClassGenerationSupport {
    private static final Logger LOG = Logger.getInstance(IDELightClassGenerationSupport.class);

    public static IDELightClassGenerationSupport getInstanceForIDE(@NotNull Project project) {
        return (IDELightClassGenerationSupport) ServiceManager.getService(project, LightClassGenerationSupport.class);
    }

    private final Project project;

    public IDELightClassGenerationSupport(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public LightClassConstructionContext analyzeRelevantCode(@NotNull Collection<JetFile> files) {
        KotlinCacheManager cacheManager = KotlinCacheManager.getInstance(project);
        KotlinDeclarationsCache declarationsCache = cacheManager.getPossiblyIncompleteDeclarationsForLightClassGeneration();
        return new LightClassConstructionContext(declarationsCache.getBindingContext(), null);
    }

    @NotNull
    @Override
    public Collection<JetClassOrObject> findClassOrObjectDeclarations(@NotNull FqName fqName, @NotNull GlobalSearchScope searchScope) {
        return JetFullClassNameIndex.getInstance().get(fqName.asString(), project, kotlinSourcesAndBinaries(searchScope));
    }

    @NotNull
    @Override
    public Collection<JetFile> findFilesForPackage(@NotNull final FqName fqName, @NotNull GlobalSearchScope searchScope) {
        Collection<JetFile> files = JetAllPackagesIndex.getInstance().get(fqName.asString(), project, kotlinSourcesAndBinaries(searchScope));
        return ContainerUtil.filter(files, new Condition<JetFile>() {
            @Override
            public boolean value(JetFile file) {
                return fqName.equals(JetPsiUtil.getFQName(file));
            }
        });
    }

    @NotNull
    @Override
    public Collection<JetClassOrObject> findClassOrObjectDeclarationsInPackage(
            @NotNull FqName packageFqName, @NotNull GlobalSearchScope searchScope
    ) {
        return JetClassByPackageIndex.getInstance().get(packageFqName.asString(), project, kotlinSourcesAndBinaries(searchScope));
    }

    @Override
    public boolean packageExists(
            @NotNull FqName fqName, @NotNull GlobalSearchScope scope
    ) {
        return !JetAllPackagesIndex.getInstance().get(fqName.asString(), project, kotlinSourcesAndBinaries(scope)).isEmpty();
    }

    @NotNull
    @Override
    public Collection<FqName> getSubPackages(@NotNull FqName fqn, @NotNull GlobalSearchScope scope) {
        Collection<JetFile> files = JetAllPackagesIndex.getInstance().get(fqn.asString(), project, kotlinSourcesAndBinaries(scope));

        Set<FqName> result = Sets.newHashSet();
        for (JetFile file : files) {
            FqName fqName = JetPsiUtil.getFQName(file);

            assert QualifiedNamesUtil.isSubpackageOf(fqName, fqn) : "Registered package is not a subpackage of actually declared package:\n" +
                                                                    "in index: " + fqn + "\n" +
                                                                    "declared: " + fqName;
            FqName subpackage = QualifiedNamesUtil.plusOneSegment(fqn, fqName);
            if (subpackage != null) {
                result.add(subpackage);
            }
        }

        return result;
    }

    @Nullable
    @Override
    public PsiClass getPsiClass(@NotNull JetClassOrObject classOrObject) {
        VirtualFile virtualFile = classOrObject.getContainingFile().getVirtualFile();
        if (virtualFile != null && LibraryUtil.findLibraryEntry(virtualFile, classOrObject.getProject()) != null) {
            return getLightClassForDecompiled((JetFile) classOrObject.getContainingFile());
        }

        return KotlinLightClassForExplicitDeclaration.create(classOrObject.getManager(), classOrObject);
    }

    @Nullable
    @Override
    public PsiClass getPsiClassForDecompiledNamespaceFile(@NotNull JetFile decompiledFile) {
        return getLightClassForDecompiled(decompiledFile);
    }

    @NotNull
    public MultiMap<String, FqName> getAllPossiblePackageClasses(@NotNull GlobalSearchScope scope) {
        Collection<String> packageFqNames = JetAllPackagesIndex.getInstance().getAllKeys(project);

        MultiMap<String, FqName> result = new MultiMap<String, FqName>();
        for (String packageFqName : packageFqNames) {
            FqName packageClassFqName = PackageClassUtils.getPackageClassFqName(new FqName(packageFqName));
            result.putValue(packageClassFqName.shortName().asString(), packageClassFqName);
        }

        return result;
    }

    @Nullable
    private static PsiClass getLightClassForDecompiled(final JetFile file) {
        VirtualFile vFile = file.getVirtualFile();
        if (vFile == null) {
            return null;
        }
        Project project = file.getProject();

        PsiManager manager = PsiManager.getInstance(project);

        final PsiJavaFileStubImpl javaFileStub = new PsiJavaFileStubImpl(file.getPackageName(), true);
        javaFileStub.setPsiFactory(new ClsWrapperStubPsiFactory());

        ClsFileImpl fakeFile = new ClsFileImpl((PsiManagerImpl) manager, new ClassFileViewProvider(manager, vFile)) {
                    @NotNull
                    @Override
                    public PsiClassHolderFileStub getStub() {
                        return javaFileStub;
                    }
                };

        fakeFile.setPhysical(false);
        javaFileStub.setPsi(fakeFile);

        Stack<StubElement> stubStack = new Stack<StubElement>();
        stubStack.push(javaFileStub);

        try {
            new ClassReader(vFile.contentsToByteArray()).accept(
                    new StubBuildingVisitor<VirtualFile>(vFile, VirtualFileInnerClassStrategy.INSTANCE, javaFileStub, 0), ClassReader.SKIP_CODE);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // TODO unbalanced stack?
        return javaFileStub.getClasses()[0];
    }

    private static class VirtualFileInnerClassStrategy implements InnerClassSourceStrategy<VirtualFile> {
        public static VirtualFileInnerClassStrategy INSTANCE = new VirtualFileInnerClassStrategy();

        @Nullable
        @Override
        public VirtualFile findInnerClass(String innerName, VirtualFile outerClass) {
            final String baseName = outerClass.getNameWithoutExtension();
            final VirtualFile dir = outerClass.getParent();
            assert dir != null;

            return dir.findChild(baseName + "$" + innerName + ".class");
        }

        @Nullable
        @Override
        public ClassReader readerForInnerClass(VirtualFile innerClass) {
            try {
                return new ClassReader(innerClass.contentsToByteArray());
            }
            catch (IOException e) {
                return null;
            }
        }
    }

}
