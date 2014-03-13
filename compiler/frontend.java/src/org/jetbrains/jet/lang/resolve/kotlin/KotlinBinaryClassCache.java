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

package org.jetbrains.jet.lang.resolve.kotlin;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.SLRUCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KotlinBinaryClassCache {

    // This cache must be small: we only query the same file a few times in a row (from different places)
    // Since it is on application level we should be careful about this cache. Consider profiling multiple projects indexing simultaneously.
    private final SLRUCache<VirtualFile, Ref<VirtualFileKotlinClass>> cache = new SLRUCache<VirtualFile, Ref<VirtualFileKotlinClass>>(2, 2) {
        @NotNull
        @Override
        public Ref<VirtualFileKotlinClass> createValue(VirtualFile virtualFile) {
            return Ref.create(VirtualFileKotlinClass.create(virtualFile));
        }
    };

    @Nullable
    public static KotlinJvmBinaryClass getKotlinBinaryClass(@NotNull VirtualFile file) {
        KotlinBinaryClassCache service = ServiceManager.getService(KotlinBinaryClassCache.class);
        synchronized (service.cache) {
            return service.cache.get(file).get();
        }
    }
}
