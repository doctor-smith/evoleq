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
package org.drx.evoleq.math


inline fun <reified C, reified D: C>
consumeConfig(
    consumer: D,
    noinline consume: C.()->Unit
): D {
    consumer.consume()
    return consumer
}


inline fun <C,D:C> D.inject(conf: D.(C.()->Unit)->D): D = this.conf{}
