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

package org.jetbrains.jet.plugin;

import com.google.common.collect.Lists;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.asm4.Type;
import org.jetbrains.jet.asJava.LightClassUtil;
import org.jetbrains.jet.codegen.AsmUtil;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptor;
import org.jetbrains.jet.lang.descriptors.NamespaceDescriptor;
import org.jetbrains.jet.lang.psi.JetClassOrObject;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiUtil;
import org.jetbrains.jet.lang.resolve.java.JavaDescriptorResolver;
import org.jetbrains.jet.lang.resolve.java.JvmClassName;
import org.jetbrains.jet.lang.resolve.java.mapping.KotlinToJavaTypesMap;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.DeferredType;
import org.jetbrains.jet.lang.types.ErrorUtils;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.TypeUtils;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import java.util.LinkedList;

public class JetPluginUtil {
    @NotNull
    private static LinkedList<String> computeTypeFullNameList(JetType type) {
        if (type instanceof DeferredType) {
            type = ((DeferredType)type).getActualType();
        }
        DeclarationDescriptor declarationDescriptor = type.getConstructor().getDeclarationDescriptor();

        LinkedList<String> fullName = Lists.newLinkedList();
        while (declarationDescriptor != null && !(declarationDescriptor instanceof ModuleDescriptor)) {
            fullName.addFirst(declarationDescriptor.getName().asString());
            declarationDescriptor = declarationDescriptor.getContainingDeclaration();
        }
        assert fullName.size() > 0;
        if (JavaDescriptorResolver.JAVA_ROOT.asString().equals(fullName.getFirst())) {
            fullName.removeFirst();
        }
        return fullName;
    }

    public static boolean checkTypeIsStandard(JetType type, Project project) {
        if (KotlinBuiltIns.getInstance().isAny(type) || KotlinBuiltIns.getInstance().isNothingOrNullableNothing(type) || KotlinBuiltIns.getInstance().isUnit(type) ||
             KotlinBuiltIns.getInstance().isFunctionOrExtensionFunctionType(type)) {
            return true;
        }

        LinkedList<String> fullName = computeTypeFullNameList(type);
        if (fullName.size() == 3 && fullName.getFirst().equals("java") && fullName.get(1).equals("lang")) {
            return true;
        }

        JetScope libraryScope = KotlinBuiltIns.getInstance().getBuiltInsScope();

        DeclarationDescriptor declaration = type.getMemberScope().getContainingDeclaration();
        if (ErrorUtils.isError(declaration)) {
            return false;
        }
        while (!(declaration instanceof NamespaceDescriptor)) {
            declaration = declaration.getContainingDeclaration();
            assert declaration != null;
        }
        return libraryScope == ((NamespaceDescriptor) declaration).getMemberScope();
    }

    @NotNull
    public static String getPluginVersion() {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("org.jetbrains.kotlin"));
        assert plugin != null : "How can it be? Kotlin plugin is available, but its component is running. Complete nonsense.";
        return plugin.getVersion();
    }

    @Nullable
    public static PsiClass getJavaAnalogOrLightClass(@NotNull JetClassOrObject classOrObject) {
        if (LightClassUtil.belongsToKotlinBuiltIns((JetFile) classOrObject.getContainingFile())) {
            Name className = classOrObject.getNameAsName();
            assert className != null : "Class from BuiltIns should have a name";
            ClassDescriptor classDescriptor = KotlinBuiltIns.getInstance().getBuiltInClassByName(className);
            Type javaAnalog = KotlinToJavaTypesMap.getInstance().getJavaAnalog(classDescriptor.getDefaultType());
            if (javaAnalog != null) {
                if (AsmUtil.isPrimitive(javaAnalog)) {
                    javaAnalog = KotlinToJavaTypesMap.getInstance().getJavaAnalog(TypeUtils.makeNullable(classDescriptor.getDefaultType()));
                    assert javaAnalog != null : "Java analog should exists for primitive nullable type";
                }
                if (javaAnalog.getSort() != Type.OBJECT) {
                    return null;
                }
                String fqName = JvmClassName.byType(javaAnalog).getFqName().asString();
                return JavaPsiFacade.getInstance(classOrObject.getProject()).
                        findClass(fqName, GlobalSearchScope.allScope(classOrObject.getProject()));
            }
        }
        if (!JetPsiUtil.isLocalClass(classOrObject)) {
            return LightClassUtil.getPsiClass(classOrObject);
        }
        return null;
    }
}
