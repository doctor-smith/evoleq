import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version Config.Versions.kotlin
}

group = "org.drx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(Config.Dependencies.coroutines)

    testCompile("junit", "junit", "4.12")
    compile(kotlin("reflect"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    sourceSets.create("examples"){
        java.srcDirs("src/examples/java")


    }

    sourceSets{
        getByName("examples"){
            java {
                compileClasspath += sourceSets["main"].output
                runtimeClasspath += sourceSets["main"].output
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
    }


}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


/* compile(Config.Dependencies.tornadofx)
                    compile(Config.Dependencies.kotlinStandardLibrary)
                    compile(Config.Dependencies.coroutines) */