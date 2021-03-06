/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.quickfix;

import com.intellij.codeInspection.HintAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * A fix with the user information hint.
 */
public abstract class JetHintAction<T extends PsiElement> extends JetIntentionAction<T> implements HintAction {

    public JetHintAction(@NotNull T element) {
        super(element);
    }

    protected boolean isCaretNearRef(Editor editor, T ref) {
        TextRange range = element.getTextRange();
        int offset = editor.getCaretModel().getOffset();

        return offset == range.getEndOffset();
    }
}
