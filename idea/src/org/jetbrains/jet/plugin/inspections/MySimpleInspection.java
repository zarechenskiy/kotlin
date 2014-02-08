/*
 * Copyright 2010-2014 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetCallExpression;
import org.jetbrains.jet.lang.psi.JetDotQualifiedExpression;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetVisitorVoid;

public class MySimpleInspection extends LocalInspectionTool implements CustomSuppressableInspectionTool {
    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "simple";
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "simple group";
    }

    @Override
    public boolean isEnabledByDefault() {
        return super.isEnabledByDefault(); // TODO
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return super.getDefaultLevel(); // TODO
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            @NotNull final ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session
    ) {
        return new JetVisitorVoid() {
            @Override
            public void visitDotQualifiedExpression(@NotNull JetDotQualifiedExpression expression) {
                JetExpression selector = expression.getSelectorExpression();
                if (selector instanceof JetCallExpression) {
                    JetCallExpression callExpression = (JetCallExpression) selector;
                    JetExpression callee = callExpression.getCalleeExpression();
                    if (callee != null && callee.getText().equals("get")) {
                        holder.registerProblem(
                                callee,
                                "Use [...] instead of get(...)",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                new LocalQuickFix() {
                                    @NotNull
                                    @Override
                                    public String getName() {
                                        return "Replace get() with []";
                                    }

                                    @NotNull
                                    @Override
                                    public String getFamilyName() {
                                        return getName();
                                    }

                                    @Override
                                    public void applyFix(
                                            @NotNull Project project, @NotNull ProblemDescriptor descriptor
                                    ) {
                                        System.out.println("done");
                                    }
                                }
                        );
                    }
                }
            }
        };
    }

    @Nullable
    @Override
    public SuppressIntentionAction[] getSuppressActions(@Nullable PsiElement element) {
        return new SuppressIntentionAction[] {};
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element) {
        return false;
    }
}
