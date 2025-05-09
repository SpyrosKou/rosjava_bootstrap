package org.ros2.catking_package_msg

import groovy.xml.XmlParser
import org.apache.commons.io.FileUtils
import org.ros2.internal.message.MessageConstants

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function
import java.util.stream.Collectors

/*
 * Generates ROS Java message definitions from all the the catkin packages found in the folders ROS_PACKAGE_PATH.
 * For instance this in ROS Noetic this can be :
 * - In Windows C:\opt\ros\noetic\x64\share
 * - In Linux  /opt/ros/noetic/share
 *
 * Implementation details:
 * The structure of this class
 * - catkinPluginRoot.catkinPackage : information about this package
 * - catkinPluginRoot.workspaces : list of Strings
 * - catkinPluginRoot.catkinPackagesTree.generate() : create the pkgs dictionary
 * - catkinPluginRoot.catkinPackagesTree.pkgs : dictionary of CatkinPackage objects
 *
 * The latter can be iterated over for information:
 *
 * catkinPluginRoot.catkinPackagesTree.catkinPackages.each { pair ->
 *   catkinPackage = pair.value
 *   println catkinPackage.name
 *   println catkinPackage.version
 *   catkinPackage.dependencies.each { d ->
 *     println d
 *   }
 *   // filtered list of *_msg dependencies.
 *   catkinPackage.getMessageDependencies().each { d ->
 *     println d
 *   }
 * }
 *
 * Use this only once in the root of a multi-project gradle build - it will
 * only generate the properties once and share them this way.
 */

final class JavaMsgGenerationManager {
//    private static final Set<String> INTERFACE_DIRECTORIES = CopyOnWriteArraySet.of(MessageConstants.MSG, MessageConstants.SRV, MessageConstants.ACTION)
    public static final String MESSAGE_INTERFACES_GROUP_ID = "org.ros2.rosjava_messages"
    private static final String TARGET_PATH = System.getProperty("user.dir") + File.separator + "build" + File.separator + "project";
    private static final String RELATIVE_GENERATED_SRC_PATH = "src" + File.separator + "generated-sources" + File.separator + "java"
    private static final String BUILD_GRADLE = "build.gradle"
    private static final String SETTINGS_GRADLE = "settings.gradle"
    private static final String README_MD = "README.md"
    private static final String MESSAGE_GENERATION_VERSION = "0.1.0"
    /*
     * Possibly should check for existence of these properties and
     * be lazy if they're already defined.
     */
    final CatkinPluginRoot catkinPluginRoot = new CatkinPluginRoot();

    static final void main(String[] args) {
        final JavaMsgGenerationManager javaMsgGenerationManager = new JavaMsgGenerationManager();
        javaMsgGenerationManager.apply(TARGET_PATH)

    }


    static final countInterfaces(final Path parentPath, final String folderName, final String requiredPostfix) {
        def findResult = 0;
        final Path path = parentPath.resolve(folderName);
        final File folder = path.toFile();
        if (folder.exists() && folder.isDirectory()) { final java.io.FileFilter filter = input ->
            input.getAbsolutePath().endsWith("." + requiredPostfix);
            findResult = Arrays
                    .stream(folder.listFiles(filter))
                    .filter(File::exists)
                    .filter(File::isFile)
                    .count();
        }

        return findResult;
    }

    def void apply(final String targetPath) {
        new File(targetPath).mkdirs();
        println("Generating files at:" + targetPath)
        this.catkinPluginRoot.workspaces.addAll("$System.env.ROS_PACKAGE_PATH".split(File.pathSeparator))
        this.catkinPluginRoot.catkinPackagesTree = new CatkinPackages(this.catkinPluginRoot, catkinPluginRoot.workspaces)
        this.catkinPluginRoot.catkinPackagesTree.generate()
        generateJavaSourcesForAllInterfacePackages(this.catkinPluginRoot, targetPath)
        //printAllPackages()

        printInterfacePackages(this.catkinPluginRoot);

        //printAllInterfacePackagesWithMissingDependencies();

    }


    final void generateRosMessages(final CatkinPackages.CatkinPackage catkinPackage, final String targetDirectoryBasis) {

        def useFileSeparator = !targetDirectoryBasis.endsWith(File.separator)
        def basePackagePath = targetDirectoryBasis + (useFileSeparator ? File.separator : "") + catkinPackage.name
        final File targetDir = new File(basePackagePath + File.separator + RELATIVE_GENERATED_SRC_PATH)
//        final File targetDir = new File(targetDirectoryBasis + (useFileSeparator ? File.separator : "") + catkinPackage.name + File.separator + "src" + File.separator + "generated-sources" + File.separator + "java" + File.separator + "org" + File.separator + "ros" + File.separator + "rosjava_messages" + File.separator + catkinPackage.name)

        final File interfaceSources = new File(catkinPackage.directory)

        final List<File> sources = List.of(interfaceSources)
//        final List<String> packages = List.of(MESSAGE_INTERFACES_GROUP_ID + "." + catkinPackage.name)
        final List<String> packages = List.of(catkinPackage.name)
        targetDir.mkdirs()

        boolean allSourcesOk = true;
        sources.forEach { dir ->
            if (!dir.exists() || !dir.isDirectory()) {
                System.err.println("Source not found:" + dir)
                allSourcesOk = false;
            }
        }

//        assert allSourcesOk "Some sources do not exist"
        def org.ros2.internal.message.GenerateInterfaces interfacesGenerator = new org.ros2.internal.message.GenerateInterfaces();
        interfacesGenerator.generate(targetDir, packages, sources);
        //println("Successes:" + interfacesGenerator.getSuccessfulInterfaceGenerations() + " Failures:" + interfacesGenerator.getFailedInterfaceGenerations() + " Total:" + interfacesGenerator.getTotalInterfaceGenerations());
        getProjectGradleBuild(catkinPackage, basePackagePath)
    }

    private static final String getFullProjectGradleBuild(final CatkinPluginRoot catkinPluginRoot, final String basePackagePath) {
        {
            final String licence =
                    """
        /*
         *  Copyright (C) 2011 Google Inc. 
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
         
         group "org.ros2.rosjava_messages"
         description "This project creates ROS Java Interfaces for ROS Noetic messages"
        
        
        subprojects {
             apply plugin: 'maven-publish'
             apply plugin: 'java-library'

            dependencies {
                compileOnly 'org.ros2.rosjava_bootstrap:message_generation:0.1.0'
                compileOnly 'io.netty:netty:3.10.6.Final'
            }
            
            compileJava {
              sourceCompatibility = JavaVersion.VERSION_17
              targetCompatibility = JavaVersion.VERSION_17
            }
            java {
              withSourcesJar()
              withJavadocJar()
            }

            repositories {
                mavenCentral()
                mavenLocal()
                maven {
                    url "https://github.com/SpyrosKou/rosjava_mvn_repo/raw/noetic"
                }
            }
            
            publishing {
                final repositoryDirectory = "ROS_MAVEN_DEPLOYMENT_REPOSITORY"
                final mavenDeploymentRepositoryProvider = providers.gradleProperty(repositoryDirectory)
                final mavenDeploymentRepository  = mavenDeploymentRepositoryProvider.getOrElse(null)

                publications {
                    mavenJava(MavenPublication) {
                        from components.java
                    }
                }
                if (mavenDeploymentRepository!=null) {
                    repositories {
                        maven {
                            name "GithubRepositoryLocation"
                            url 'file:////' + mavenDeploymentRepository
                        }
                    }
                } else {
                    logger.debug(repositoryDirectory + "not found")
                }
            }
        }
        """

            final String content = licence
            final File gradleFile = new File(basePackagePath + File.separator + BUILD_GRADLE)
            FileUtils.writeStringToFile(gradleFile, content, Charset.defaultCharset());
        }
        {
            final String licence =
                    """
/*
 *  Copyright (C) 2011 Google Inc. 
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
         
"""
            final StringJoiner stringJoiner = new StringJoiner("'\n include '", " include '", "'\n")
            catkinPluginRoot.catkinPackagesTree.catkinPackages.values()
                    .stream()
                    .filter(CatkinPackages.CatkinPackage::hasInterfaceDefinitions)
                    .map(CatkinPackages.CatkinPackage::getName)
                    .forEach(stringJoiner::add)

            final String content = licence + "\n" + stringJoiner.toString()
            final File gradleFile = new File(basePackagePath + File.separator + SETTINGS_GRADLE)
            FileUtils.writeStringToFile(gradleFile, content, Charset.defaultCharset());
        }

        {
            final String prePackages =
                    """
# ROS Java Messages, Services and Actions for [ROS Noetic Ninjemys](http://wiki.ros.org/noetic) 
This repository contains the source code for building the ROS Java interfaces for ROS Java Noetic.

# Usage of the generated java artifacts and dependencies
The java artifacts have two `compileOnly` gradle dependency that translates into `runtime` dependencies in maven
These are:   
- 'org.ros2.rosjava_bootstrap:message_generation:0.1.0'.   
- 'io.netty:netty:3.10.6.Final'.   
   
It is possible to use these ROS Java artifacts with different implementations and/or versions of these dependencies.   
This is a design decision to allow compatibility with multiple rosjava implementations.

    

  
# List of Interfaces included
The following table lists all the interfaces compiled into ROS Java Interfaces.    


The table lists:


-  The package name
-  The version used for compilation
-  The package dependencies
-  A list of urls related to each package, such as links to the source, issues or wikis
-  A short description 
-  The number of ROS messages compiled
-  The number of ROS services compiled
-  The number of ROS actions compiled



Package Name | Version | Dependencies | Url | Description
--- | --- | --- |---|---"""
//Package Name | Version | Dependencies | Url | Description| Messages |Services|Actions
//--- | --- | --- |---|---|---  |---|---"""

            final String postTable =
                    """
# Publishing to a Maven Repository in the filesystem.
If an environment variable named `ROS_MAVEN_DEPLOYMENT_REPOSITORY` exists and is not blank, then 
the rosjava interfaces artifacts can be deployed in this file system repository.

In particular, a maven repository named `FileSystemMaven`will be added together with the related tasks. If the environment variable `ROS_MAVEN_DEPLOYMENT_REPOSITORY` does not exist or if its content is a null or empty String, these tasks will not be created.
"""
            final StringJoiner packagesJoiner = new StringJoiner("\n")
            catkinPluginRoot
                    .catkinPackagesTree
                    .catkinPackages.values()
                    .stream()
                    .filter(CatkinPackages.CatkinPackage::hasInterfaceDefinitions)
                    .map(CatkinPackages.CatkinPackage::toString)
                    .forEach(packagesJoiner::add)


            final String content = prePackages + "\n" + packagesJoiner.toString()
            final File gradleFile = new File(basePackagePath + File.separator + README_MD)
            FileUtils.writeStringToFile(gradleFile, content, Charset.defaultCharset());
        }
    }

/**
 *
 * @param catkinPackage
 * @param basePackagePath
 * @return
 */
    private final String getProjectGradleBuild(final CatkinPackages.CatkinPackage catkinPackage, final String basePackagePath) {

        final Set<CatkinPackages.CatkinPackage> messageDependencies = catkinPackage
                .dependencies
                .stream()
                .distinct()
                .map(name -> this.catkinPluginRoot.catkinPackagesTree.catkinPackages[name])
                .filter(Objects::nonNull)
                .filter(aCatkinPackage -> aCatkinPackage.hasInterfaceDefinitions())
                .collect(Collectors.toSet())
        final Set<String> dependencyLines = messageDependencies.stream().map(catkinPackageIterator -> {
            if (this.catkinPluginRoot.catkinPackagesTree.catkinPackages.containsKey(catkinPackageIterator.name)) {
                /* println("  Internal: " + d) */
                return "    api project(\":" + catkinPackageIterator.name + "\")"
            } else {
                /* println("  External: " + d) */
                return "    api org.ros2.rosjava_messages:" + catkinPackageIterator + ":" + catkinPackageIterator.version + "+"
            }
        }).collect(Collectors.toSet());
        final String licence =
                """
/*
 *  Copyright (C) 2011 Google Inc. 
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
 
 
"""
        final List<String> bodyDefinitions = new ArrayList<>();
        {
            bodyDefinitions.add("group \"" + MESSAGE_INTERFACES_GROUP_ID + "\"")
            bodyDefinitions.add("version " + "\"" + catkinPackage.version + "\"")
            bodyDefinitions.add("description " + "\"ROS Message Interfaces Generation for " + catkinPackage.name + ":" + catkinPackage.version + "\"")
            bodyDefinitions.add("\nsourceSets.main.java.srcDirs " + "\"src\" + File.separator + \"generated-sources\" + File.separator + \"java\"")

        }
        final StringJoiner dependenciesJoiner = new StringJoiner("\n", "dependencies{\n", "\n}")
        dependencyLines.forEach(dependenciesJoiner::add)
        final StringJoiner bodyDefinitionsJoiner = new StringJoiner("\n", "", "\n")
        bodyDefinitions.forEach(bodyDefinitionsJoiner::add)
        def dependenciesPart = dependencyLines.isEmpty() ? "" : dependenciesJoiner.toString()
        final String content = licence + "\n" + bodyDefinitionsJoiner.toString() + "\n" + dependenciesPart + "\n"
        (new File(basePackagePath)).mkdirs()
        final File gradleFile = new File(basePackagePath + File.separator + BUILD_GRADLE)
        FileUtils.writeStringToFile(gradleFile, content, Charset.defaultCharset());
    }


    private final void generateJavaSourcesForAllInterfacePackages(final CatkinPluginRoot catkinPluginRoot, final String targetDirectory) {

        catkinPluginRoot.catkinPackagesTree.catkinPackages.each { catkinPackage ->
            if (catkinPackage.value.hasInterfaceDefinitions()) {
                generateRosMessages(catkinPackage.value, targetDirectory)
            }
        }
        getFullProjectGradleBuild(catkinPluginRoot, targetDirectory)
    }

    final static void printInterfacePackages(final CatkinPluginRoot catkinPluginRoot) {
        println("All Interfaces of Catkin Workspaces........." + catkinPluginRoot.workspaces)
        println("Catkin Interface Packages {")
        catkinPluginRoot.catkinPackagesTree.catkinPackages.each { catkinPackage ->
            if (catkinPackage.value.hasInterfaceDefinitions()) {
                println(" " + catkinPackage.value.toString())
            }
        }
        println("}")
    }

    def void printAllPackages() {
        println("Catkin Workspaces........." + catkinPluginRoot.workspaces)
        println("Catkin Packages")
        this.catkinPluginRoot.catkinPackagesTree.catkinPackages.each { catkinPackage -> println(catkinPackage.value.toString() + " interfaces?: " + catkinPackage.packageHasInterfaceDefinitions())
        }
    }

    def void printAllInterfacePackagesWithMissingDependencies() {
        println("All Interfaces of Catkin Workspaces........." + catkinPluginRoot.workspaces)
        println("Catkin Interface Packages that miss interfaces{")
        this.catkinPluginRoot.catkinPackagesTree.catkinPackages.each { catkinPackage ->

            if (catkinPackage.packageHasInterfaceDefinitions() && !this.catkinPluginRoot.catkinPackagesTree
                    .catkinPackages
                    .keySet()
                    .containsAll(catkinPackage.value.dependencies.stream().filter(name -> this.catkinPluginRoot.catkinPackagesTree[name].packageHasInterfaceDefinitions()).collect(Collectors.toSet()))) {
                println(" " + catkinPackage.value.toString())
            }
        }
        println("}")
    }


}

final class CatkinPluginRoot {
    final CatkinPackages.CatkinPackage catkinPackage
    final Set<String> workspaces = new HashSet<>()
    CatkinPackages catkinPackagesTree
}

final class CatkinPackages {

    private static final String MESSAGE_GENERATION = "message_generation"

    Map<String, CatkinPackage> catkinPackages
    Set<String> workspaces
    CatkinPluginRoot catkinPluginRoot

    CatkinPackages(CatkinPluginRoot catkinPluginRoot, Set<String> workspaces) {
        this.catkinPluginRoot = catkinPluginRoot
        this.workspaces = workspaces
        this.catkinPackages = [:]
    }

    void generate() {
        //create only once
        if (this.catkinPackages.size() == 0) {
            this.workspaces.each { workspace ->
//                println("Parsing Workspace:" + workspace)
                if (workspace != null && "null" != workspace) {
                    var pathForWorkspace = Paths.get(workspace)
                    if (pathForWorkspace == null || pathForWorkspace.isEmpty()) {
                        System.err.println("workspace:" + workspace + " has path:" + pathForWorkspace);
                    } else {
                        System.out.println("workspace:" + workspace + " has path:" + pathForWorkspace);
                        Files.walk(pathForWorkspace)
                                .filter(path -> path.toFile().isDirectory() && path.toFile().exists())
                                .map(directory -> directory.resolve("package.xml").toFile())
                                .filter(Objects::nonNull)
                                .filter(File::exists)
                                .filter(File::isFile)
//                        .peek(file -> System.out.println("File:{" + file.getAbsolutePath() + "}"))
                                .forEach({ file ->
//                            println("Parsing: " + file.getAbsolutePath())
                                    def catkinPackage = new CatkinPackage(catkinPluginRoot, file)
                                    if (this.catkinPackages.containsKey(catkinPackage.name)) {
                                        if (this.catkinPackages[catkinPackage.name].version < catkinPackage.version) {
                                            println("Catkin generate tree: replacing older version of " + catkinPackage.name + "[" + this.catkinPackages[catkinPackage.name].version + "->" + catkinPackage.version + "]")
                                            catkinPackages[catkinPackage.name] = catkinPackage
                                        }
                                    } else {
                                        catkinPackages.put(catkinPackage.name, catkinPackage)
                                    }
                                });
                    }
                }
            }
        } else {
            println("ROS Workspaces already parsed. Known packages:" + this.catkinPackages.size());
        }
    }

    final class CatkinPackage {
        final CatkinPluginRoot catkinPluginRoot
        final String name
        final String version
        final Set<String> dependencies
        final String directory
        final String sourceDirectoryName
        final List<String> url
        final String description
        final int msg;
        final int action;
        final int srv;

        CatkinPackage(final CatkinPluginRoot catkinPluginRoot, final File packageXmlFile) {
            this.catkinPluginRoot = catkinPluginRoot
//        println "Loading " + packageXmlFile
            def packageXml = new XmlParser().parse(packageXmlFile)
            this.directory = packageXmlFile.getParentFile().getAbsolutePath()
            this.name = packageXml.name.text()
            this.sourceDirectoryName = packageXmlFile.getParentFile().getName()
            this.version = packageXml.version.text()
            this.url = packageXml.url.collect { it.text() }
            this.description = packageXml.description.text().replaceAll("\n", " ")
            final List<String> build_dependencies = packageXml.build_depend.collect { it.text() }
            final List<String> run_dependencies = packageXml.run_depend.collect { it.text() }
            final List<String> just_dependencies = packageXml.depend.collect { it.text() }
            final Path parentPath = packageXmlFile.parentFile.toPath();

            this.msg = JavaMsgGenerationManager.countInterfaces(parentPath, MessageConstants.MSG, MessageConstants.MSG)
            this.srv = JavaMsgGenerationManager.countInterfaces(parentPath, MessageConstants.SRV, MessageConstants.SRV)
            this.action = JavaMsgGenerationManager.countInterfaces(parentPath, MessageConstants.ACTION, MessageConstants.ACTION)
            def temp_dependencies = new HashSet<>()
            temp_dependencies.addAll(build_dependencies)
            temp_dependencies.addAll(run_dependencies)
            temp_dependencies.addAll(just_dependencies)
            this.dependencies = Collections.unmodifiableSet(temp_dependencies)
        }

/**
 * Determines whether the package contains ROS Message. ROS Service or ROS Action definitions
 *
 * @param package_name
 * @return
 */
        final Boolean hasInterfaceDefinitions() {
            final boolean considerDependencies = false
            final boolean considerFolders = false
            final boolean considerInterfaceCount = true
            final boolean printNote = false

            def result = false
            try {
                def catkinPackage = this.catkinPluginRoot.catkinPackagesTree.catkinPackages[name]
                if (considerDependencies) {
                    catkinPackage.dependencies.each { dependency ->
                        if (dependency.equalsIgnoreCase(MESSAGE_GENERATION)) {
                            result = true
                        }
                    }
                }
                final File directoryFile = new File(catkinPackage.directory)
                def Boolean hasInterface = false;
                if (!result && directoryFile.exists() && directoryFile.isDirectory()) {
                    final Path parentPath = directoryFile.toPath();



                    if (considerFolders) {
                        INTERFACE_DIRECTORIES.forEach(interfaceDirectoryName -> {
                            //Skip processing if already determined as an interface
                            if (!hasInterface) {
                                final File file = parentPath.resolve(interfaceDirectoryName).toFile();
                                hasInterface = hasInterface || file.exists() && file.isDirectory() && file.canRead() && file.listFiles(new FilenameFilter() {
                                    @Override
                                    boolean accept(File dir, String name) {
                                        return dir.getAbsolutePath().equals(file.getAbsolutePath()) && name.endsWith("." + interfaceDirectoryName);
                                    }
                                }).size() > 0
                            }
                        })
                    }

                    def interfaceDefinitionFound = false
                    if (considerInterfaceCount) {
                        interfaceDefinitionFound = (this.msg + this.srv + this.action) > 0;
                    }

                    def isMessagePackage = result || hasInterface || interfaceDefinitionFound;
                    if (printNote && isMessagePackage && (!result || !hasInterface || !interfaceDefinitionFound)) {
                        println("Note: " + package_name + " has messageGeneration:" + result + " interfaceFolders:" + hasInterface + " interfaceDefinitionFound:" + interfaceDefinitionFound)
                    }
                    return isMessagePackage;
                }

            } catch (NullPointerException e) {
                /* Not a catkin package dependency (e.g. boost), ignore */
                result = false
            }
            return result
        }

        final String toString() {
            final String dependenciesList = toBullets(dependencies, Function.identity())

//        final String urls=toBullets(url,x->"<a href="+x+">"+x+"</a>")
            final String urls = toBullets(url, Function.identity())
            final String result = "${name} | ${version} |${dependenciesList} |${urls} |${description}"
//            final String result = "${name} | ${version} |${dependenciesList} |${urls} |${description} |${msg}| ${srv} | ${action}"
            return result
        }

        private static final String toBullets(final Collection<String> strings, final Function<String, String> wrapper) {

            final StringJoiner stringJoiner = new StringJoiner(" <BR> ")
            strings.stream().map(wrapper).forEach(stringJoiner::add)
            return stringJoiner.toString();
        }

    }

}