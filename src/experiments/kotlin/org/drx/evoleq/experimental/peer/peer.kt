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
package org.drx.evoleq.experimental.peer

import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.flow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.flow.Flow
import org.drx.evoleq.gap.Filling
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.Spatula



sealed class SpatulaFillGap<W,P>
data class SpatulaFillGapRequest<W,P>(val gap: Gap<W, P>) : SpatulaFillGap<W,P>()
data class SpatulaFillGapResponse<W,P>(val f: (W) -> Evolving<W>)  : SpatulaFillGap<W,P>()

fun <W,P> catch(spatula: Spatula<W, P>): Flow<SpatulaFillGap<W, P>, Boolean> = flow{
    conditions(once())
    flow{
        when (it) {
            is SpatulaFillGapRequest<W,P> -> Immediate{
                SpatulaFillGapResponse<W,P>(
                    spatula.fill(it.gap)
                )
            }
            else -> Immediate{it}
        }
    }
}


sealed class FillingFillGap<W,P>
data class FillingFillGapRequest<W,P>(val gap: Gap<W, P>) : FillingFillGap<W,P>()
data class FillingFillGapResponse<W,P>(val f: suspend (W) -> Evolving<W>)  : FillingFillGap<W,P>()

fun <W,P> catch(filling: Filling<P>): Flow<FillingFillGap<W, P>, Boolean> = flow{
    conditions(once())
    flow{
        when (it) {
            is FillingFillGapRequest<W,P> -> Immediate{
                FillingFillGapResponse<W,P>(
                    filling.fill(it.gap)
                )
            }
            else -> Immediate{it}
        }
    }
}
