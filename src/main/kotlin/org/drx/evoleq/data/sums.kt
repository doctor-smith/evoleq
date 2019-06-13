package org.drx.evoleq.data



sealed class Sum<D,E> {
    data class First<D,E>(val value: D): Sum<D,E>()
    data class Second<D,E>(val value: E): Sum<D,E>()
}

sealed class TripleSum<D,E,F>  {
    data class First<D,E,F>(val value: D): TripleSum<D,E,F>()
    data class Second<D,E,F>(val value: E): TripleSum<D,E,F>()
    data class Third<D,E,F>(val value: F): TripleSum<D,E,F>()
}

sealed class QuadSum<D,E,F,G>  {
    data class First<D,E,F,G>(val value: D): QuadSum<D,E,F,G>()
    data class Second<D,E,F,G>(val value: E): QuadSum<D,E,F,G>()
    data class Third<D,E,F,G>(val value: F): QuadSum<D,E,F,G>()
    data class Fourth<D,E,F,G>(val value: G): QuadSum<D,E,F,G>()
}

sealed class QuintSum<D,E,F,G,H>  {
    data class First<D,E,F,G,H>(val value: D): QuintSum<D,E,F,G,H>()
    data class Second<D,E,F,G,H>(val value: E): QuintSum<D,E,F,G,H>()
    data class Third<D,E,F,G,H>(val value: F): QuintSum<D,E,F,G,H>()
    data class Fourth<D,E,F,G,H>(val value: G): QuintSum<D,E,F,G,H>()
    data class Fith<D,E,F,G,H>(val value: H): QuintSum<D,E,F,G,H>()
}



fun <D,E,F> Sum<Sum<D,E>,F>.flatten(): TripleSum<D,E,F> = when(this) {
    is Sum.First -> when(this.value) {
        is Sum.First -> TripleSum.First(this.value.value)
        is Sum.Second -> TripleSum.Second(this.value.value)
    }
    is Sum.Second -> TripleSum.Third(this.value)
}