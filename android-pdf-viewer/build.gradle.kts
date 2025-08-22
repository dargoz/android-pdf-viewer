plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.jfrog.artifactory")
    id("maven-publish")
}


android {
    compileSdk = 35
    namespace = "com.github.dargoz"
    version = "3.1.0"
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

var dependencyResult: Map<String, String> = mutableMapOf()
val dependencyList by tasks.register("printResolvedVersions") {

    doFirst {
        // find configuration list that can be resolved
        configurations.asMap.forEach { (s, configuration) ->
            if (configuration.isCanBeResolved) {
                println("configurations $s: ${configuration.isCanBeResolved}")
            }
        }
        val dependencyList = mutableListOf<List<String>>()
        configurations["releaseCompileClasspath"].incoming.resolutionResult.allDependencies.forEach { dependency ->
            if (dependency.requested.displayName.contains("compose") && dependency.isConstraint) {
                // isConstraint means that the dependency managed by BoM
                val dependencyComponent = dependency.requested.displayName.split(":")
                dependencyList.add(dependencyComponent)
                println("dependency = ${dependency.requested.displayName}, match = ${dependency.isConstraint}")
            }
        }
        dependencyResult = dependencyList.associate {
            Pair(
                first = "${it.first()}:${it[1]}", // dependency full name
                second = it[2].split(" ").last().replace("}", "") // dependency version
            )
        }
        dependencyResult.forEach { (displayName, version) ->
            println("final dependency = $displayName , version = $version")
        }

    }
}

afterEvaluate {
    tasks {
        /*named<org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask>("artifactoryPublish") {
            publications("publishAar")
        }*/
        named("generatePomFileForPublishAarPublication") {
            dependsOn(dependencyList)
        }
    }

}

configure<PublishingExtension> {

    publications {
        group = "com.github.dargoz"
        register<MavenPublication>("publishAar") {
            artifact("${layout.buildDirectory.asFile.get()}/libs/android-pdf-viewer-$version-sources.jar") {
                classifier = "sources"
            }
            artifact("${layout.buildDirectory.asFile.get()}/outputs/aar/android-pdf-viewer-release.aar")
            artifactId = project.name
            version = project.version.toString()
            pom.withXml {
                println("pom with xml : $dependencyResult")
                val dependenciesNode = asNode().appendNode("dependencies")
                val configNames = listOf("implementation", "api")
                configNames.forEach { cfgName ->
                    configurations.getByName(cfgName) {
                        dependencies.forEach {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", it.group)
                            dependencyNode.appendNode("artifactId", it.name)

                            if (it.version == null) {
                                dependencyNode.appendNode(
                                    "version",
                                    dependencyResult["${it.group}:${it.name}"]
                                )
                            } else {
                                dependencyNode.appendNode("version", it.version)
                            }

                            if (it.name == "android-pdf-viewer" || it.name == "pdfium-android") {
                                dependencyNode.appendNode("type", "aar")
                            }
                        }
                    }
                }
            }
        }
    }

}


dependencies {
    implementation("androidx.core:core:1.12.0")
    implementation("com.github.dargoz:pdfium-android:2.1.0@aar")
}
