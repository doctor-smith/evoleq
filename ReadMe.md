[ ![Download](https://api.bintray.com/packages/drx/maven/evoleq/images/download.svg?version=1.0.3) ](https://bintray.com/drx/maven/evoleq/1.0.3/link)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
# Evoleq

Functional programming in terms of dynamical systems. A declarative approach to application design.
## Goals
* Write applications (and their functional flows) using a process-dsl.
* Unify synchronous and asynchronous coding  
* Arrange asynchronous, concurrent components of applications in an unbreakable process hierarchy.
* Design applications in a really composable way.

## Build your Project with Evoleq 
Evoleq is hosted on bintray/jcenter
### Gradle
```kotlin
compile( "org.drx:evoleq:1.0.3" )
```
### Maven
```xml
<dependency>
    <groupId>org.drx</groupId>
    <artifactId>evoleq</artifactId>
    <version>1.0.3</version>
</dependency>
```
### Notes
   * Evoleq targets JVM 1.8

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
It is obvious that implementations of the Evolving type are monadic when they are functorial.
The implementations provided in this library are all functorial.

The well-disposed reader sees immediately that the evolution map is a co-algebra.

## Terminology 

### Flows, Stubs and Gaps
Use stubs, gaps and flows to organize the evolution maps:

#### Example: First one wins
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
val result = flow.evolve(0).get() // 2
```


#### Example (schematic): Intercept application shutdown with a confirmation dialog
```kotlin
/**
 * Application stub 
 */
val appStub = stub<Message> {
    evolve{ message -> when( message ) {
        is Message.Start -> TODO()
       
        // interception
        is Message.Close -> message.intercept(
            closeDialogStub,
            gap{
                from { message -> Parallel{ Dialog.Show } }
                to { message, dialog -> when(dialog) {
                    is Dialog.Ok -> Immedaite{ Message.CloseResponse }
                    is Dialog.Cancel -> Immediate{ Message.Resume }
                    else -> Immediate{ Message.Resume }
                } }
            }     
       )
             
       is Message.Resume -> TODO()
       is Mesage.CloseResponse -> Immediate{ message }
    } }
}    
/**
 * Aplication flow
 */
val appFlow = appStub.toFlow<Message, Boolean>(
    conditions{
        testObject(true)
        check{ b -> b }
        updateCondition{ message -> message !is Message.CloseReponse }
    }
)
```

For convenience, we also give the definitions of the used data types

```kotlin
/**
 * Dialog message
 */
sealed class Dialog {
    object Show : Dialog()
    object Ok : Dialog()
    object Cancel : Dialog()
}
/**
 * Dialog stub
 */
val closeDialogStub: Stub<Dialog> = TODO()


/**
 * Application Messages
 */
sealed class Message {
    object Start : Message()
    object Close : Message()
    object CloseResponse : Message()
    object Resume : Message()
}
```


#### Example (schematic): Error handling

```kotlin
val stub = stub<D>{
    evolve{ data -> throw(Exception("Error Message")) }
}


val stub2 = stub.runCatchingErrors()
val d: D = TODO()
val result = stub2.evolve(ErrorCatcher(d)).get().error // thrown exception

```

#### Example (schematic): Handle incoming messages
```kotlin
val port: Actor<Message> = actor<Message>()

class Data<D>(val d: D, val message: Message = EmptyMessage)

val receiver = receivingStub<Data,Message>{
    receiver(port)
    gap{
        from { data: Data -> Immediate{ data.message } } 
        to { data, message -> Immediate{ data.copy( message= message) }}
    }
}


val flow = receiver.toFlow<Data, Boolean>(
    conditions{
        testObject( true )
        check { b->b }
        updateCondition{ data -> data.message <= 1000 }
    }
)

val run = flow.evolve( d )

GlobalScope.launch{
    Parallel<Unit>{
        (1..1000).forEach{ actor.send(Message(it)) }
    }
}

val result = run.get() // Data(d, 1000)

```
<!--
Example (schematic): History

Suppose 

```kotlin

```
To be continued...
-->
## Examples 
Take a look at the sources of [evoleq-examples](https://bitbucket.org/dr-smith/evoleq-examples/src/master/) 

## Versions
Take a look at the [version history](VERSIONS.md).


