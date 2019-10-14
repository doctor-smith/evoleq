/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.evoleq.flow

interface Phase

sealed class Process<D>(open val data: D) : Phase {
    /**
     * Phases of the process
     */
    sealed class Phase<D>(override val data: D) : Process<D>(data) {
        /**
         * Start-up phase
         */
        sealed class StartUp<D>(override val data: D) : Phase<D>(data) {
            /**
             *
             */
            data class Start<D>(override val data: D) : StartUp<D>(data)

            /**
             *
             */
            data class Stop<D>(override val data: D) : StartUp<D>(data)
        }

        /**
         * Runtime phase
         */
        sealed class Runtime<D>(override val data: D) : Phase<D>(data) {
            /**
             *
             */
            class Wait<D>(override val data: D) : Runtime<D>(data)

            /**
             *
             */
            class Stop<D>(override val data: D) : Runtime<D>(data)
        }

        /**
         * Termination phase
         */
        sealed class Termination<D>(override val data: D) : Phase<D>(data) {
            /**
             *
             */
            data class Stop<D>(override val data: D) : Termination<D>(data)

            /**
             *
             */
            data class Dead<D>(override val data: D) : Termination<D>(data)
        }
    }

    /**
     * Output of the process - sent to bgOutput-receiver
     */
    sealed class Output<D>(override val data: D) : Process<D>(data) {

        /**
         * Process is starting
         */
        data class Starting<D>(override val data: D) : Output<D>(data)

        /**
         * Process runtime is waiting for input
         */
        data class Waiting<D>(override val data: D) : Output<D>(data)

        /**
         *
         */
        data class Stopped<D>(override val data: D) : Output<D>(data)

        /**
         *
         */
        data class StoppedWithError<D>(override val data: D, val throwable: Throwable? = null) : Output<D>(data)

        /**
         *
         */
        data class Error<D>(override val data: D, val throwable: Throwable? = null) : Output<D>(data)
    }
}