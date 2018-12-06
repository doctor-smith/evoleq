package org.drx.evoleq.gap

import org.drx.evoleq.data.Immediate

fun unitGap(): Gap<Unit, Unit> =
    Gap({ u: Unit -> Immediate { u } }, { u: Unit -> { v: Unit -> Immediate { v } } })
fun <D> initGap(d:D): Gap<Unit, D> =
    Gap({ Immediate { d } }, { { d: D -> Immediate {} } })
fun <D> selfGap(): Gap<D, D> =
    Gap({ d: D -> Immediate { d } }, { d: D -> { e: D -> Immediate { e } } })