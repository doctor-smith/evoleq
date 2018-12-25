/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.gap

import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.evolving.Evolving

open class Filling<P>(private val filler: suspend (P)->Evolving<P>) {
    suspend fun <W> fill(gap: Gap<W,P>): suspend (W)->Evolving<W> = suspended( gap.fill(filler) )
}