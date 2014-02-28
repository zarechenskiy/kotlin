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

package org.jetbrains.jet.cfg;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.cfg.PseudocodeVariablesData;
import org.jetbrains.jet.lang.cfg.pseudocode.Instruction;
import org.jetbrains.jet.lang.cfg.pseudocode.Pseudocode;
import org.jetbrains.jet.lang.cfg.pseudocode.PseudocodeImpl;
import org.jetbrains.jet.lang.cfg.pseudocodeTraverser.Edges;
import org.jetbrains.jet.lang.descriptors.VariableDescriptor;
import org.jetbrains.jet.lang.resolve.BindingContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.jetbrains.jet.lang.cfg.PseudocodeVariablesData.VariableInitState;
import static org.jetbrains.jet.lang.cfg.PseudocodeVariablesData.VariableUseState;

public abstract class AbstractVariablesDataTest extends AbstractControlFlowTest {

    @Override
    protected void checkEdgesDirections(Pseudocode pseudocode) {
    }

    @Override
    public void dumpInstructions(PseudocodeImpl pseudocode, @NotNull StringBuilder out, BindingContext bindingContext) {
        PseudocodeVariablesData pseudocodeVariablesData = new PseudocodeVariablesData(pseudocode.getTopMostParentOrThis(), bindingContext);
        Map<Instruction, Edges<Map<VariableDescriptor, VariableInitState>>> variableInitializers =
                pseudocodeVariablesData.getVariableInitializers();
        Map<Instruction, Edges<Map<VariableDescriptor, VariableUseState>>> useStatusData =
                pseudocodeVariablesData.getVariableUseStatusData();
        List<Instruction> instructions = pseudocode.getAllInstructions();
        int maxLength = countMaxInstructionLength(instructions);
        for (Instruction instruction : instructions) {
            Edges<Map<VariableDescriptor, VariableInitState>> initializersEdges = variableInitializers.get(instruction);
            Edges<Map<VariableDescriptor, VariableUseState>> useStateEdges = useStatusData.get(instruction);
            out.append(formatInstruction(instruction, maxLength, ""));
            out.append("    INIT:");
            if (initializersEdges != null) {
                dumpEdgesData(initializersEdges, out);
            }
            out.append("    USE:");
            if (useStateEdges != null) {
                dumpEdgesData(useStateEdges, out);
            }
            out.append("\n");
        }
    }

    private <D> void dumpEdgesData(@NotNull Edges<Map<VariableDescriptor, D>> edges, @NotNull StringBuilder out) {
        out.append(" in: ").append(renderVariableMap(edges.getIn()))
           .append("    out: ").append(renderVariableMap(edges.getOut()));
    }

    private <D> String renderVariableMap(Map<VariableDescriptor, D> map) {
        List<String> result = Lists.newArrayList();
        for (Map.Entry<VariableDescriptor, D> entry : map.entrySet()) {
            VariableDescriptor variable = entry.getKey();
            D data = entry.getValue();
            result.add(variable.getName() + "=" + data);
        }
        Collections.sort(result);
        return "{" + StringUtil.join(result, ", ") + "}";
    }
}
