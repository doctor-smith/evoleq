object Config {

    object ProjectData {
        val group = "org.drx"
        val version = "1.1.0"
        val artifactId = "evoleq"
    }

    object Versions {

        val kotlin = "1.3.30"
        val coroutines = "1.2.0"


        val tornadofx  = "1.7.18"

        val junit = "4.12"
    }

    object Dependencies {
        val kotlinStandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"


        val tornadofx  = "no.tornado:tornadofx:${Versions.tornadofx}"

    }




}
