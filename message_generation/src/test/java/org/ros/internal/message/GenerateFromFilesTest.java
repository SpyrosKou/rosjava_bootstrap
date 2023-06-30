package org.ros.internal.message;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created at 2022-06-18 on 01:52
 *
 * @author Spyros Koukas
 */
public class GenerateFromFilesTest {
    private static final String targetDir = "build/generated-sources/java";
    private static final String sourcesDir = "src/test/resources/";
    private static File targetDirFile;

    private final List<File> packageDirectories = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        final File file = new File(sourcesDir);
        Assumptions.assumeTrue(file.exists());
        Assumptions.assumeTrue(!file.isFile());
        Assumptions.assumeTrue(file.isDirectory());

        this.targetDirFile = new File(targetDir);
        targetDirFile.mkdirs();
        Assumptions.assumeTrue(targetDirFile.exists());
        Assumptions.assumeTrue(!targetDirFile.isFile());
        Assumptions.assumeTrue(targetDirFile.isDirectory());
        this.packageDirectories.addAll(Arrays.stream(file.listFiles(File::isDirectory)).toList());
    }

    @AfterEach
    public void clear() {
        this.packageDirectories.clear();
        this.targetDirFile = null;
    }

    @Test
    public void testRosMessageCreationOfFiles() {


        final List<String> packages = new ArrayList();
        final List<File> sources = new ArrayList();
        for (final File packageDirectory : packageDirectories) {

            sources.add(packageDirectory);


            packages.add(packageDirectory.getName());
        }
        final GenerateInterfaces interfacesGenerator = new GenerateInterfaces();
        interfacesGenerator.generate(targetDirFile, packages, sources);
    }

}


