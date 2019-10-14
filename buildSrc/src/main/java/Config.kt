object Config {

    object ProjectData {
        val group = "org.drx"


        /**
         * version 1.1.0:
         *  - OnDemand<D> : Evolving<D>: block execution is delayed until get-method is called
         *  - Background flow
         *  - structured concurrency
         *  - Lazy evolvings, stubs, flows, etc
         *  - breaking: Refactored block in Immediate: Its type is no longer suspending
         */
        val version = "1.1.0"
        val artifactId = "evoleq"
    }

    object Versions {

        val kotlin = "1.3.50"
        val coroutines = "1.3.1"


        val tornadofx  = "1.7.18"

        val junit = "4.12"
    }

    object Dependencies {
        val kotlinStandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"


        val tornadofx  = "no.tornado:tornadofx:${Versions.tornadofx}"

    }




}
