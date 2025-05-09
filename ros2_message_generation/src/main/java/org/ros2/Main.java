package org.ros2;/*
 * Copyright (C) 2025 Spyros Koukas.
 * Copyright (C) 2011 Google Inc.
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
import com.google.common.collect.Lists;
import org.ros2.internal.message.GenerateInterfaces;

import java.io.File;
import java.util.List;

public final class Main {
    static private final String ROS_PACKAGE_PATH = "ROS_PACKAGE_PATH";
    public static final void main(String[] args) {
        final List<String> arguments = Lists.newArrayList(args);
        if (arguments.size() == 0) {
            arguments.add(".");
        }

        String rosPackagePath = System.getenv(ROS_PACKAGE_PATH);
        // Overwrite with a supplied package path if specified (--package-path=)
        for (final String argument : arguments) {
            if (argument.contains("--package-path=")) {
                rosPackagePath = argument.replace("--package-path=", "");
                break;
            }
        }

        final List<File> packagePath = Lists.newArrayList();
        for (final String path : rosPackagePath.split(File.pathSeparator)) {
            final File packageDirectory = new File(path);
            if (packageDirectory.exists()) {
                packagePath.add(packageDirectory);
            }
        }

        final GenerateInterfaces generateInterfaces = new GenerateInterfaces();
        final File outputDirectory = new File(arguments.remove(0));
        generateInterfaces.generate(outputDirectory, arguments, packagePath);
//        System.out.println("Successes:" + generateInterfaces.getSuccessfulInterfaceGenerations() + " Failures:" + generateInterfaces.getFailedInterfaceGenerations() + " Total:" + generateInterfaces.getTotalInterfaceGenerations());
    }
}
