package org.drx.evoleq.math

import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.Parallel

//infix fun <S,T> Parallel<S>.mapParallel(f: (S)->T): Parallel<T> = scope.parallel { f(get()) }
infix fun <S,T> Parallel<S>.mapParallel(f: suspend (S)->T): Parallel<T> = scope.parallel { f(get()) }

/**
 * TODO handle scopes / jobs in a convenient way
 */
suspend fun <D> muParallel(parallel: Parallel<Parallel<D>>): Parallel<D> = parallel.scope.parallel { parallel.get() }.get()
//parallel.get()
/*parallel.scope.parallel{

    val inner =parallel.get()
    inner.get()
}
*/

/**
 * TODO handle scopes / jobs in a convenient way
 */
suspend infix fun <R,S,T> (suspend (R)->Parallel<S>).fishParallel(arrow: suspend  (S)->Parallel<T>): suspend  (R)->Parallel<T> = {
    r -> muParallel(this(r) mapParallel arrow)
}
