object Config {

    object ProjectData {
        val group = "org.drx"
        val version = "1.0.2"
        val artifactId = "evoleq"
    }

    object Versions {

        val kotlin = "1.3.20"
        val coroutines = "1.1.1"

        val pipeline = "1.0.4"

        val jfxmobile = "1.3.11"
        val retrolambda = "3.7.0" //"+"
        val javafxports = "8.60.9"

        val tornadofx  = "1.7.18"
        val tornadofxAndroidCompat = "1.0.1"

        val grpc = "1.15.1"
        val protobuf = "0.8.6"

        val androidPlugin = "3.2.1"

        val junit = "4.12"



    }

    object Dependencies {
        val kotlinStandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"

        val pipeline = "org.drx:pipeline-kt:${Versions.pipeline}"

        val jfxmobile = "org.javafxports:jfxmobile-plugin:${Versions.jfxmobile}"
        val retrolambda = "gradle.plugin.me.tatarka:gradle-retrolambda:${Versions.retrolambda}"
        val orfjacklRetrolambdaConfig = "net.orfjackal.retrolambda:retrolambda:+"

        val tornadofx  = "no.tornado:tornadofx:${Versions.tornadofx}"
        val tornadofxAndroidCompat = "no.tornado:tornadofx-android-compat:${Versions.tornadofxAndroidCompat}"

        val junit = "junit:junit:${Versions.junit}"
        val grpcTesting = "io.grpc:grpc-testing:${Config.Versions.grpc}"

        val androidToolsGradle = "com.android.tools.build:gradle:${Versions.androidPlugin}"
    }

    object Repositories {
        val drxMavenSnapshots = "http://drx.maven.net:8081/repository/snapshots"

        val drxBintray = "https://dl.bintray.com/drx/maven"

        val gradlePluginsM2 = "https://plugins.gradle.org/m2/"

        val jitpackIo = "https://jitpack.io"

        val googleMvn = "https://maven.google.com"


    }

}
