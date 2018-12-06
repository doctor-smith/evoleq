package org.drx.evoleq.flow

import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.conditions.Counter
import org.drx.evoleq.conditions.counting
import org.drx.evoleq.evolve

suspend fun<D> repeatParallel(times: Long,initialData: D, f:(D)->D):D =
    evolve<Pair<D, Long>, Long>(
        initialData = Pair(initialData, 0),
        conditions = Counter<D>(times).get()
    ) { pair ->
        Parallel { counting(f)(pair) }
    }.first
suspend fun<D> repeatImmediate(times: Long,initialData: D, f:(D)->D):D =
    evolve<Pair<D, Long>, Long>(
        initialData = Pair(initialData, 0),
        conditions = Counter<D>(times).get()
    ) { pair ->
        Immediate { counting(f)(pair) }
    }.first

