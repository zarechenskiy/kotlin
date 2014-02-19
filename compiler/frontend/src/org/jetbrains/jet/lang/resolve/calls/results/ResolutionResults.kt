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

package org.jetbrains.jet.lang.resolve.calls.results.ResolutionResults

import org.jetbrains.jet.lang.descriptors.CallableDescriptor
import org.jetbrains.jet.lang.resolve.calls.model.ResolvedCall
import org.jetbrains.jet.lang.resolve.calls.results.OverloadResolutionResultsImpl
import java.util.Collections

public class ResolutionResults<D : CallableDescriptor>(
        val resolutionResults: OverloadResolutionResultsImpl<D>,
        allCandidates: Collection<ResolvedCall<out D>>,
        private val collectAllCandidates: Boolean
) {
    val allCandidates = if (collectAllCandidates) allCandidates else Collections.emptyList()
      get() = if (collectAllCandidates) $allCandidates else throw IllegalStateException("The candidates were not collected.")

    fun changeResolutionResults(resolutionResults: OverloadResolutionResultsImpl<D>) =
            ResolutionResults(resolutionResults, $allCandidates, collectAllCandidates)
}

public fun <D : CallableDescriptor> create(
        results: OverloadResolutionResultsImpl<D>,
        candidates: Collection<ResolvedCall<out D>>,
        collectAllCandidates: Boolean
): ResolutionResults<D> = ResolutionResults<D>(results, candidates, collectAllCandidates)

public fun <D : CallableDescriptor> create(
        results: OverloadResolutionResultsImpl<D>,
        collectAllCandidates: Boolean
): ResolutionResults<D> = create(results, Collections.emptyList(), collectAllCandidates)
