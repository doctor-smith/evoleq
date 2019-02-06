# A declarative approach to application design
Think of your applications (and their functional flow) being easily described in a dsl-flavoured fashion.
Think of your applications asynchronous, concurrent components being arranged in an unbreakable process hierarchy, which is easily understood.
Think of your applications being designed in a really composable way 


## The Core

The functional heart of the library consists of just one powerful function (and a variation for suspended flows).
### The main function
```kotlin
package org.drx.evoleq

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.conditions.ok
import org.drx.evoleq.conditions.update
import org.drx.evoleq.evolving.Evolving

/**
 * Evolution
 */
tailrec suspend fun <D, T> evolve(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    flow: (D) -> Evolving<D>
) : D = when( conditions.ok() ) {
    false -> initialData
    true -> {
        val evolvedData: D = flow ( initialData ).get()
        evolve(
            evolvedData,
            conditions.update( evolvedData )
        ){
            data -> flow ( data )
        }
    }
}
```  
### The main data type
```kotlin
package org.drx.evoleq.evolving

interface Evolving<out D> {
    suspend fun get() : D
}

```

It is obvious that the Evolving type is monadic. 



