import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version Config.Versions.kotlin
    id ("com.github.hierynomus.license") version "0.15.0"
    `maven-publish`
    maven
    id ("com.jfrog.bintray") version "1.8.0"
}

group = Config.Evoleq.group
version = Config.Evoleq.version//"1.0.0-beta"//-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(Config.Dependencies.coroutines)
    compile(kotlin("reflect"))

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    sourceSets.create("examples"){
        java.srcDirs("src/examples/java")
    }
    sourceSets.create("experiments"){
        java.srcDirs("src/experiments/java")
    }

    sourceSets{
        getByName("examples"){
            java {
                compileClasspath += sourceSets["main"].output
                runtimeClasspath += sourceSets["main"].output
            }
        }
        getByName("experiments"){
            java {
                compileClasspath += sourceSets["main"].output
                runtimeClasspath += sourceSets["main"].output
            }
        }
        getByName("test"){
            java {
                compileClasspath += sourceSets["experiments"].output
                runtimeClasspath += sourceSets["experiments"].output
            }
        }
    }
}
kotlin{
    sourceSets {
        getByName("examples"){
            kotlin.srcDirs("src/examples/kotlin")
            configurations {
                dependencies{
                    implementation(Config.Dependencies.tornadofx)
                    implementation(Config.Dependencies.kotlinStandardLibrary)
                    implementation(Config.Dependencies.coroutines)
                    implementation("io.reactivex.rxjava2:rxkotlin:2.2.0")
                }
            }
        }
        getByName("experiments"){
            kotlin.srcDirs("src/experiments/kotlin")
            configurations {
                dependencies{
                    implementation(Config.Dependencies.tornadofx)
                    implementation(Config.Dependencies.kotlinStandardLibrary)
                    implementation(Config.Dependencies.coroutines)
                    //implementation("io.reactivex.rxjava2:rxkotlin:2.2.0")
                }
            }
        }
    }


}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks {

    val sourceSets: SourceSetContainer by project

    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        classifier = "javadoc"
        from(tasks["javadoc"])
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }


}

task("writeNewPom") {
    doLast {
        maven.pom {
            withGroovyBuilder {
                "project" {
                   // setProperty("inceptionYear", "2008")
                    "licenses" {
                        "license" {
                            setProperty("name", "The Apache Software License, Version 2.0")
                            setProperty("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                            setProperty("distribution", "repo")
                        }
                    }
                }
            }
        }.writeTo("$buildDir/pom.xml")
        //println("$buildDir")
    }
}

publishing {
    (publications) {
        "EvoleqPublication"(MavenPublication::class) {
            artifactId = Config.Evoleq.artifactId
            groupId = Config.Evoleq.group
            from (components["java"])

            artifact (tasks.getByName("sourcesJar")) {
                classifier = "sources"
            }

            artifact (tasks.getByName("javadocJar")) {
                classifier = "javadoc"
            }



            pom.withXml {
                val root = asNode()
                root.appendNode("description", "Evoleq")
                root.appendNode("name", Config.Evoleq.artifactId)
                root.appendNode("url", "https://bitbucket.org/dr-smith/evoleq.git")
                root.children().addAll(maven.pom().dependencies)
            }

/*
            pom.withXml {
                asNode().appendNode("dependencies").let { depNode ->
                    configurations.compile.allDependencies.forEach {
                        depNode.appendNode("dependency").apply {
                            appendNode("groupId", it.group)
                            appendNode("artifactId", it.name)
                            appendNode("version", it.version)
                        }
                    }
                }
            }
*/
        }
    }
}




bintray {
    user = project.properties["bintray.user"] as String
    key = project.properties["bintray.key"] as String

    publish = true
    override = true

    //setPublications("EvoleqPublication")

    pkg (delegateClosureOf<BintrayExtension.PackageConfig>{
        repo = "maven"
        name = "evoleq"
        description = ""
        //userOrg = user
        vcsUrl = "https://bitbucket.org/dr-smith/evoleq.git"
        setLabels("kotlin", "coroutine", "dynamical system", "recursive store", "evolution equation")
        setLicenses("Apache-2.0")

        version (delegateClosureOf<BintrayExtension.VersionConfig>{
            name = Config.Evoleq.version
            //desc = "build ${build.number}"
            //released  = Date(System.currentTimeMillis())
            gpg (delegateClosureOf<BintrayExtension.GpgConfig>{
                sign = true
            })
        })
    })

}
/*
fun MavenPom.addDependencies() = withXml {
    asNode().appendNode("dependencies").let { depNode ->
        configurations.compile.allDependencies.forEach {
            depNode.appendNode("dependency").apply {
                appendNode("groupId", it.group)
                appendNode("artifactId", it.name)
                appendNode("version", it.version)
            }
        }
    }
}
*/
