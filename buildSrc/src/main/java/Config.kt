object Config {

    object ProjectData {
        val group = "org.drx"
        val version = "1.0.3"
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


        val tornadofx  = "no.tornado:tornadofx:${Versions.tornadofx}"

    }




}
