package org.drx.evoleq.math

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.dsl.flow
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Flow
import org.drx.evoleq.flow.SuspendedFlow
import org.drx.evoleq.flow.fill
import org.drx.evoleq.gap.Gap


fun <D,S,E,T> Flow<D, S>.then(
    phi: Flow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (Flow<E,T>)-> Flow<D,T> = {flow: Flow<E,T> -> gap.fill(flow)}
    val evolveThis: suspend (D)-> Evolving<D> = { d:D -> this@then.evolve(d) }
    val evolvePhi: suspend (D)-> Evolving<D> = { d:D -> filler(phi).evolve(d) }
    val combined: suspend (D)->Evolving<D> = {d: D -> (evolveThis * evolvePhi)(d)}
    flow = {d ->combined(d)}
}

fun <D,S,E,T> SuspendedFlow<D, S>.then(
    phi: Flow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (Flow<E,T>)-> Flow<D,T> = {flow: Flow<E,T> -> gap.fill(flow)}
    val evolvePhi: suspend (D)-> Evolving<D> = { d:D -> filler(phi).evolve(d) }
    val combined: suspend (D)->Evolving<D> = {d: D -> (this@then.flow * evolvePhi)(d)}
    flow = {d ->combined(d)}
}

fun <D,S,E,T> Flow<D, S>.then(
    phi: SuspendedFlow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (SuspendedFlow<E,T>)-> SuspendedFlow<D,T> = {flow: SuspendedFlow<E,T> -> gap.fill(flow)}
    val evolveThis: suspend (D)-> Evolving<D> = { d:D -> this@then.evolve(d) }
    val combined: suspend (D)->Evolving<D> = {d: D -> (evolveThis * filler(phi).flow)(d)}
    flow = {d ->combined(d)}
}

fun <D,S,E,T> SuspendedFlow<D, S>.then(
    phi: SuspendedFlow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (SuspendedFlow<E,T>)-> SuspendedFlow<D,T> = {flow: SuspendedFlow<E,T> -> gap.fill(flow)}
    val combined: suspend (D)->Evolving<D> = {d: D -> (this@then.flow * filler(phi).flow)(d)}
    flow = {d ->combined(d)}
}

