package org.jetbrains.jet.completion;

import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.jet.plugin.PluginTestCaseBase;
import org.jetbrains.jet.plugin.project.TargetPlatform;

import java.io.File;

public abstract class AbstractKotlinSourceInJavaCompletionTest extends JetFixtureCompletionBaseTestCase {
    @Override
    public TargetPlatform getPlatform() {
        return TargetPlatform.JVM;
    }

    @Override
    public void doTest(String testPath) {
        File mockLibDir = new File(PluginTestCaseBase.getTestDataPathBase() + "/completion/injava/mockLib");
        File[] listFiles = mockLibDir.listFiles();
        assertNotNull(listFiles);
        String[] paths = ArrayUtil.toStringArray(ContainerUtil.map(listFiles, new Function<File, String>() {
            @Override
            public String fun(File file) {
                return file.getAbsolutePath();
            }
        }));
        myFixture.configureByFiles(paths);
        super.doTest(testPath);
    }
}
