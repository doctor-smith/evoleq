# Evoleq

Functional programming in terms of dynamical systems. A declarative approach to application design.
## Goals
* Write applications (and their functional flow) in a dsl-flavoured fashion.
* Arrange applications asynchronous, concurrent components in an unbreakable process hierarchy.
* Design applications in a really composable way.


## The Core 

The functional heart of the library consists of just one powerful function (and a variation for suspended flows).
### The Main Function 
```kotlin
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
The main idea behind the following data type is to provide a uniform way to treat synchronous and asynchronous processes.  
```kotlin
package org.drx.evoleq.evolving

interface Evolving<out D> {
    suspend fun get() : D
}

```
It is obvious that the Evolving type is monadic.
 

<!--## Outlook
-->
 
## Terminology <!-- / Code snippets-->

### Flows, Stubs and Gaps
Use stubs, gaps and flows to organize the evolution maps:

Example: First one wins
```kotlin
val stub = racingStub<Int,Int> {
    timeout (1_000 )
    // drivers
    driver{ Parallel{
        delay(150)
        1
    }}
    driver{ Parallel{
        delay(100)
        2
    }}
    driver{ Parallel{
        delay(10)
        3
    }}
    // gap
    gap{
        from{ Immediate{ null } }
        to{x , y-> Immediate{
            when(y==null){
                true -> x
                false -> y
            }
        }}
    }
}
val flow = stub.toFlow( conditions( once() ))
val result = flow.evolve(0).get()
```

To be continued...
<!--
### Gaps


### Stubs 
-->



