package org.drx.evoleq.evolving

import kotlinx.coroutines.runBlocking
import org.junit.Test

class CancellableTest {

    @Test fun cancel() = runBlocking{

        val cancellable = object : Cancellable<Int> {
            override fun cancel(d: Int): Evolving<Int> {
                return Parallel{d}
            }
        }

        val ev = cancellable.cancel(1)
        assert(ev.get() == 1)
    }

}