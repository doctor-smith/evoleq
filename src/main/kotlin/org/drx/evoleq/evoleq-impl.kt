package org.drx.evoleq

import org.drx.evoleq.conditions.Counter
import org.drx.evoleq.conditions.counting

suspend fun<D> repeatParallel(times: Long,initialData: D, f:(D)->D):D =
    evolve<Pair<D,Long>,Long>(
        initialData = Pair(initialData,0),
        conditions = Counter<D>(times).get()
    ){
            pair -> Parallel{ counting(f)(pair) }
    }.first
suspend fun<D> repeatImmediate(times: Long,initialData: D, f:(D)->D):D =
    evolve<Pair<D,Long>,Long>(
        initialData = Pair(initialData,0),
        conditions = Counter<D>(times).get()
    ){
            pair -> Immediate{ counting(f)(pair) }
    }.first

