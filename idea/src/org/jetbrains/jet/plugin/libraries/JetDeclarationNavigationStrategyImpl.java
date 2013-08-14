package org.jetbrains.jet.plugin.libraries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetDeclarationNavigationStrategy;
import org.jetbrains.jet.lang.psi.JetNamedDeclaration;

public class JetDeclarationNavigationStrategyImpl implements JetDeclarationNavigationStrategy {
    @Nullable
    @Override
    public JetNamedDeclaration getNavigationElement(@NotNull JetNamedDeclaration original) {
        return JetSourceNavigationHelper.findSourceDeclaration(original);
    }
}
