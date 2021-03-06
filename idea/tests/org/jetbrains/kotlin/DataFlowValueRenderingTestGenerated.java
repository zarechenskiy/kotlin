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

package org.jetbrains.kotlin;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.JetTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/dataFlowValueRendering")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class DataFlowValueRenderingTestGenerated extends AbstractDataFlowValueRenderingTest {
    public void testAllFilesPresentInDataFlowValueRendering() throws Exception {
        JetTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/testData/dataFlowValueRendering"), Pattern.compile("^(.+)\\.kt$"), true);
    }

    @TestMetadata("classProperty.kt")
    public void testClassProperty() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/classProperty.kt");
        doTest(fileName);
    }

    @TestMetadata("complexIdentifier.kt")
    public void testComplexIdentifier() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/complexIdentifier.kt");
        doTest(fileName);
    }

    @TestMetadata("complexIdentifierWithImplicitReceiver.kt")
    public void testComplexIdentifierWithImplicitReceiver() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/complexIdentifierWithImplicitReceiver.kt");
        doTest(fileName);
    }

    @TestMetadata("complexIdentifierWithInitiallyNullableReceiver.kt")
    public void testComplexIdentifierWithInitiallyNullableReceiver() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/complexIdentifierWithInitiallyNullableReceiver.kt");
        doTest(fileName);
    }

    @TestMetadata("complexIdentifierWithReceiver.kt")
    public void testComplexIdentifierWithReceiver() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/complexIdentifierWithReceiver.kt");
        doTest(fileName);
    }

    @TestMetadata("multipleVariables.kt")
    public void testMultipleVariables() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/multipleVariables.kt");
        doTest(fileName);
    }

    @TestMetadata("packageProperty.kt")
    public void testPackageProperty() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/packageProperty.kt");
        doTest(fileName);
    }

    @TestMetadata("receivers.kt")
    public void testReceivers() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/receivers.kt");
        doTest(fileName);
    }

    @TestMetadata("smartCast.kt")
    public void testSmartCast() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/smartCast.kt");
        doTest(fileName);
    }

    @TestMetadata("smartNotNull.kt")
    public void testSmartNotNull() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("idea/testData/dataFlowValueRendering/smartNotNull.kt");
        doTest(fileName);
    }
}
