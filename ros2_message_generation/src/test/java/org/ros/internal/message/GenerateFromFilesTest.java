/*
 * Copyright (C) 2025 Spyros Koukas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
    private static final String sourcesDir = "src/test/resources/common_interfaces";
    private static final String exampleInterfacesPackageDir = "src/test/resources/example_interfaces";
    private static File targetDirFile;

    private final List<File> packageDirectories = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        final File exampleInterfacesPackage = new File(exampleInterfacesPackageDir);
        Assumptions.assumeTrue(exampleInterfacesPackage.exists());
        Assumptions.assumeTrue(!exampleInterfacesPackage.isFile());
        Assumptions.assumeTrue(exampleInterfacesPackage.isDirectory());

        final File sourcesFile = new File(sourcesDir);
        Assumptions.assumeTrue(sourcesFile.exists());
        Assumptions.assumeTrue(!sourcesFile.isFile());
        Assumptions.assumeTrue(sourcesFile.isDirectory());

        this.targetDirFile = new File(targetDir);
        targetDirFile.mkdirs();
        Assumptions.assumeTrue(targetDirFile.exists());
        Assumptions.assumeTrue(!targetDirFile.isFile());
        Assumptions.assumeTrue(targetDirFile.isDirectory());
        this.packageDirectories.addAll(Arrays.stream(sourcesFile.listFiles(File::isDirectory)).toList());
        this.packageDirectories.add(exampleInterfacesPackage);
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


