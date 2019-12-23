[ ![Download](https://api.bintray.com/packages/drx/maven/evoleq/images/download.svg?version=1.1.1) ](https://bintray.com/drx/maven/evoleq/1.1.1/link)
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
compile( "org.drx:evoleq:1.1.1" )
```
### Maven
```xml
<dependency>
    <groupId>org.drx</groupId>
    <artifactId>evoleq</artifactId>
    <version>1.1.0</version>
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

    val job: Job

    suspend fun get() : D
}
```
It is obvious that implementations of the Evolving type are monadic when they are functorial.
The implementations provided in this library are all functorial.

## Terminology 
Evoleq is inspired by the theory of __dynamical systems__. 
Roughly speaking, a (discrete) dynamical system can be described as follows: 
if you have an invertible map 
T: X->X, 
where a is a set, 
then T induces a map
&Phi; : Z x X -> X: (n,x)->T<sup>n</sup>(x) on X (here, Z is the set of integers) 
with the following properties:
* &Phi;(0,x) = x, for all elements x of X,
* &Phi;(n+m,x) = &Phi;(n, &Phi;(m,x)) for all integers n,m and all x in X.
 
&Phi; is called the __flow__ of T on X or simply a __dynamical system__.
Further, mathematicians would say that X __evolves__ under the flow of T.

The requirement on T in being invertible is too strong for our purposes.
We will keep the terminology but allow all transformations T: X->X. Doing so, we end up with a somehow monoidal version
of flows &Phi;: N x X -> X, (n, x)->T<sup>n</sup>(x), where N is the set of non-negative integers.



### Flows, Stubs and Gaps
Use stubs, gaps and flows to organize the evolution maps:

#### Example: First one wins the race
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
        from{ OnDemand{ null } }
        to{x , y-> OnDemand{
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
                    is Dialog.Ok -> OnDemand{ Message.CloseResponse }
                    is Dialog.Cancel -> OnDemand{ Message.Resume }
                    else -> OnDemand{ Message.Resume }
                } }
            }     
       )
             
       is Message.Resume -> TODO()
       is Mesage.CloseResponse -> OnDemand{ message }
    } }
}    
/**
 * Application flow
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
        from { data: Data -> OnDemand{ data.message } } 
        to { data, message -> OnDemand{ data.copy( message= message) }}
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






