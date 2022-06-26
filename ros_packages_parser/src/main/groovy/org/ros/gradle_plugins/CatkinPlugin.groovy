package org.ros.gradle_plugins

import groovy.xml.XmlParser

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArraySet
import java.util.stream.Collectors

/*
 * Provides catkin information to the gradle build, defining properties:
 *
 * - catkinPluginRoot.pkg : information about this package
 * - catkinPluginRoot.workspaces : list of Strings
 * - catkinPluginRoot.catkinPackagesTree.generate() : create the pkgs dictionary
 * - catkinPluginRoot.catkinPackagesTree.pkgs : dictionary of CatkinPackage objects
 *
 * The latter can be iterated over for information:
 *
 * catkinPluginRoot.catkinPackagesTree.pkgs.each { pair ->
 *   pkg = pair.value
 *   println pkg.name
 *   println pkg.version
 *   pkg.dependencies.each { d ->
 *     println d
 *   }
 *   // filtered list of *_msg dependencies.
 *   pkg.getMessageDependencies().each { d ->
 *     println d
 *   }
 * }
 *
 * Use this only once in the root of a multi-project gradle build - it will
 * only generate the properties once and share them this way.
 */

class CatkinPlugin {

    static void main(String[] args) {
        println("--Starting--")
        final CatkinPlugin catkinPlugin = new CatkinPlugin();
        catkinPlugin.apply()
        println("--finished--")
    }
    /*
     * Possibly should check for existence of these properties and
     * be lazy if they're already defined.
     */
    final CatkinPluginRoot catkinPluginRoot = new CatkinPluginRoot();

    def void apply() {
        this.catkinPluginRoot.workspaces = []
        this.catkinPluginRoot.workspaces = "$System.env.ROS_PACKAGE_PATH".split(File.pathSeparator)
        this.catkinPluginRoot.catkinPackagesTree = new CatkinPackages(this.catkinPluginRoot, catkinPluginRoot.workspaces)
        this.catkinPluginRoot.catkinPackagesTree.generate()

        printAllPackages()

        printInterfacePackages();

        printAllInterfacePackagesWithMissingDependencies();

    }

    def void printInterfacePackages() {
        println("All Interfaces of Catkin Workspaces........." + catkinPluginRoot.workspaces)
        println("Catkin Interface Packages {")
        this.catkinPluginRoot.catkinPackagesTree.catkinPackages.each { catkinPackage ->
            if (this.catkinPluginRoot.catkinPackagesTree.isMessagePackage(catkinPackage.key))
                    {println(" "+catkinPackage.value.toString())}
        }
        println("}")
    }
    def void printAllPackages() {
        println("Catkin Workspaces........." + catkinPluginRoot.workspaces)
        println("Catkin Packages")
        this.catkinPluginRoot.catkinPackagesTree.catkinPackages.each { catkinPackage -> println(catkinPackage.value.toString()
                +" interfaces?: "+this.catkinPluginRoot.catkinPackagesTree.isMessagePackage(catkinPackage.key))
        }
    }

    def void printAllInterfacePackagesWithMissingDependencies() {
        println("All Interfaces of Catkin Workspaces........." + catkinPluginRoot.workspaces)
        println("Catkin Interface Packages that miss interfaces{")
        this.catkinPluginRoot.catkinPackagesTree.catkinPackages.each { catkinPackage ->

            if (this.catkinPluginRoot.catkinPackagesTree.isMessagePackage(catkinPackage.key)
               &&!this.catkinPluginRoot.catkinPackagesTree.catkinPackages.keySet().containsAll(catkinPackage.value.dependencies.stream().filter(name->this.catkinPluginRoot.catkinPackagesTree.isMessagePackage(name)).collect(Collectors.toSet())))
            {
                println(" "+catkinPackage.value.toString())
            }
        }
        println("}")
    }


}

class CatkinPluginRoot {
    CatkinPackage catkinPackage
    List<String> workspaces
    CatkinPackages catkinPackagesTree
}

class CatkinPackages {

    private static final String MESSAGE_GENERATION = "message_generation"
    private static final String MSG = "msg"
    private static final String SRV = "srv"
    private static final String ACTION = "action"
    private static final Set<String> INTERFACE_DIRECTORIES = CopyOnWriteArraySet.of(MSG, SRV, ACTION)
    Map<String, CatkinPackage> catkinPackages
    List<String> workspaces
    CatkinPluginRoot catkinPluginRoot

    CatkinPackages(CatkinPluginRoot catkinPluginRoot, List<String> workspaces) {
        this.catkinPluginRoot = catkinPluginRoot
        this.workspaces = workspaces
        this.catkinPackages = [:]
    }

    void generate() {
        //create only once
        if (this.catkinPackages.size() == 0) {
            this.workspaces.each { workspace ->
                println("Parsing Workspace:" + workspace)
                Files.walk(Paths.get(workspace))
                        .filter(path -> path.toFile().isDirectory() && path.toFile().exists())
                        .map(directory -> directory.resolve("package.xml").toFile())
                        .filter(Objects::nonNull)
                        .filter(File::exists)
                        .filter(File::isFile)
                        .peek(file -> System.out.println("File:{" + file.getAbsolutePath() + "}"))
                        .forEach(
                                { file ->
                                    println("Parsing: " + file.getAbsolutePath())
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
        }else{
            println("ROS Workspaces already parsed. Known packages:"+this.catkinPackages.size());
        }
    }

    Boolean isMessagePackage(String package_name) {

        def result = false
        try {
            def catkinPackage = this.catkinPackages[package_name]
            /* println("    Name: " + catkinPackage.name + "-" + catkinPackage.version) */
            /* println("    Dep-dependencies: " + catkinPackage.dependencies) */
            catkinPackage.dependencies.each { dependency ->
                if (dependency.equalsIgnoreCase(MESSAGE_GENERATION)) {
                    result = true
                }
            }
            final File directoryFile = new File(catkinPackage.directory)
            def Boolean hasInterface = false;
            if (!result && directoryFile.exists() && directoryFile.isDirectory()) {
                final Path path = directoryFile.toPath();
                INTERFACE_DIRECTORIES.forEach(
                        interfaceDirectoryName -> {
                            //Skip processing if already determined as an interface
                            if (!hasInterface) {
                                final File file = path.resolve(interfaceDirectoryName).toFile();
                                hasInterface = hasInterface || file.exists()
                                        && file.isDirectory()
                                        && file.canRead()
                                        && file.listFiles(new FilenameFilter() {
                                    @Override
                                    boolean accept(File dir, String name) {
                                        return dir.getAbsolutePath().equals(file.getAbsolutePath())
                                                && name.endsWith("." + interfaceDirectoryName);
                                    }
                                }).size() > 0
                            }
                        }
                )
                def isMessagePackage=result || hasInterface;
                if (isMessagePackage &&(!result||!hasInterface)){
                    println("Note: "+package_name+" has messageGeneration:"+result+" but interfaceFolders:"+hasInterface)
                }
                return result || hasInterface;
            }

        } catch (NullPointerException e) {
            /* Not a catkin package dependency (e.g. boost), ignore */
            result = false
        }
        return result
    }

    void generateMessageArtifact(CatkinPluginRoot catkinPluginRoot, String package_name) {
        def catkinPackage = this.catkinPackages[package_name]
        catkinPluginRoot.version = catkinPackage.version
        /* println("Artifact: " + catkinPackage.name + "-" + catkinPackage.version) */
        project.dependencies.add("compileOnly", 'org.ros.rosjava_bootstrap:message_generation:[0.3,0.4)')
        Set<String> messageDependencies = catkinPackage.getMessageDependencies()
        messageDependencies.each { d ->
            if (catkinPluginRoot.getParent().getChildProjects().containsKey(d)) {
                /* println("  Internal: " + d) */
                catkinPluginRoot.dependencies.add("api", project.dependencies.project(path: ':' + d))
            } else {
                /* println("  External: " + d) */
                project.dependencies.add("api", 'org.ros.rosjava_messages:' + d + ':[0.0,)')
            }
        }
        def generatedSourcesDir = "${project.buildDir}/generated-src"
        def generateSourcesTask = catkinPluginRoot.tasks.create("generateSources", JavaExec)
        generateSourcesTask.description = "Generate sources for " + catkinPackage.name
        generateSourcesTask.outputs.dir(catkinPluginRoot.file(generatedSourcesDir))
        /* generateSourcesTask.args = new ArrayList<String>([generatedSourcesDir, catkinPackage.name]) */
        generateSourcesTask.args = new ArrayList<String>([generatedSourcesDir, '--package-path=' + catkinPackage.directory, catkinPackage.name])
        generateSourcesTask.classpath = catkinPluginRoot.configurations.runtime
        generateSourcesTask.main = "org.ros.internal.message.GenerateInterfaces"
        catkinPluginRoot.tasks.compileJava.source generateSourcesTask.outputs.files
    }
}

class CatkinPackage {
    CatkinPluginRoot catkinPluginRoot
    String name
    String version
    final Set<String> dependencies=new HashSet<>();
    String directory

    CatkinPackage(CatkinPluginRoot catkinPluginRoot, File packageXmlFilename) {
        this.catkinPluginRoot = catkinPluginRoot
        println "Loading " + packageXmlFilename
        def packageXml = new XmlParser().parse(packageXmlFilename)
        directory = packageXmlFilename.parent
        name = packageXml.name.text()
        version = packageXml.version.text()
        def build_dependencies = packageXml.build_depend.collect { it.text() }
        def just_dependencies= packageXml.depend.collect { it.text() }
        dependencies.addAll(build_dependencies)
        dependencies.addAll(just_dependencies)
    }

    String toString() { "${name} ${version} ${dependencies}" }

    Set<String> getTransitiveDependencies(Collection<String> dependencies) {
        Set<String> result = [];
        dependencies.each {
            if (catkinPluginRoot.catkinPackagesTree.catkinPackages.containsKey(it)) {
                result.add(it)
                result.addAll(getTransitiveDependencies(
                        catkinPluginRoot.catkinPackagesTree.catkinPackages[it].dependencies))
            }
        }
        return result
    }

    Set<String> getMessageDependencies() {
        getTransitiveDependencies(dependencies).findAll {
            catkinPluginRoot.catkinPackagesTree.catkinPackages.containsKey(it) &&
                    catkinPluginRoot.catkinPackagesTree.catkinPackages[it].dependencies.contains("message_generation")
        } as Set
    }

}

