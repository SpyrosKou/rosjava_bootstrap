package org.ros.internal.message;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @Before
    public void setUp() {
        final File file = new File(sourcesDir);
        Assume.assumeTrue(file.exists());
        Assume.assumeTrue(!file.isFile());
        Assume.assumeTrue(file.isDirectory());

        this.targetDirFile = new File(targetDir);
        targetDirFile.mkdirs();
        Assume.assumeTrue(targetDirFile.exists());
        Assume.assumeTrue(!targetDirFile.isFile());
        Assume.assumeTrue(targetDirFile.isDirectory());
        this.packageDirectories.addAll(Arrays.stream(file.listFiles(File::isDirectory)).collect(Collectors.toList()));
    }

    @After
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


