# Evolution Equations in Functional Programming

One of the key facts in programming is that the state of the program changes with time.
An approach to handling this situation is to use an event-based design.

But it is completely unsatisfactory from an functional programmers perspective.

## How to bring change into the functional setup?
Functions, objects, arrows, functors etc are all static things. So how can we model change within this setup?
Well, let's take a look at mathematics! How did the mathematicians deal with change?
Isn't it that they are able to develop things over time using the notion of functions?
It is! Differential Equations or better: Evolution Equations.

The most simple differential equation is given by the formula 

```f' = f, f(0)= f_0 ```
 
 The solution is with initial value f_0 is given by
 
 ```f(t)= f_0*e^t```
 
 The flow of the d.e. is a map
 
 ```Phi: RxR->R: Phi(t,x)=x*e^t```
 
 So the whole set of possible solutions can be described using just one function!
 
 The core of this paper:
 
 ```kotlin

tailrec suspend fun <D,T> evolve(
    data: D,
    testObject: T,
    condition:(T)->Boolean,
    updateCondition: (D)-> T,
    flow: (D)->Deferred<D>
    ): D = when ( condition( testObject ) ) {
            false -> data
            true -> {
                val newData = flow( data ).await()
                val newTestObject = updateCondition( newData )
                evolve(
                    newData, 
                    newTestObject, 
                    condition, 
                    updateCondition, 
                    flow
                )
            }
    }

```  