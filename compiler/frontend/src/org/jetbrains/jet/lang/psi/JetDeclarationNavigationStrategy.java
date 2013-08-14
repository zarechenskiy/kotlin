package org.jetbrains.jet.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JetDeclarationNavigationStrategy {
    @Nullable
    JetNamedDeclaration getNavigationElement(@NotNull JetNamedDeclaration original);
}
