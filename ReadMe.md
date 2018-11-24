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

```
f' = f, f(0)= f_0
```
 
 The solution is with initial value f_0 is given by
 
 ```
 f(t)= f_0*e^t
 ```
 
 The flow of the d.e. is a map
 
 ```
 Phi: RxR->R: Phi(t,x)=x*e^t
 ```
 
 So the whole set of possible solutions can be described using just one function!

 The bad news: We cannot use differential equations.
 The good news: Get inspiration from theory of dynamical system. There, T:M->M is a (continuous) map between topological spaces.
 The flow of T on M is a function
 ```
 Phi: M x N -> M: (p,n) -> T^n (p)
 ```
 satisfying
 ```
 - Phi( _ , 0) = id_M
 - Phi( _ , n) o Phi(_ ,m) = Phi(_, m+n)
 ```

 => Recursion will solve our problem

 The core of this paper:
 
 ```kotlin

package org.drx.evoleq
/**
 * Evolution equation
 */
tailrec suspend fun <D, T> evolve(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    flow: suspend (D) -> Evolving<D>
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

### The data type Evolving is monadic
Evolving is a monadic type.

- Processes
- Process lenses
- Gap

## Advanced convenient usage
### Side-effects / Evolving views

```
package org.drx.evoleq

data class Gap<W,P>(
    val from:(W)->Evolving<P>,
    val to:(P)->Evolving<W>
)

interface Spatula<W,P> {
    fun fill(gap: Gap<W,P>): (W)->Evolving<W>
}

class View(gap:Gap<W,P>,...args) : Spatula {
    init {
        val filler: (P)->Evolving<P> = Parallel {
            ... UI side effects ...
        }
        fill(gap)
    }

    fun fill:(gap: Gap<W,P>): (W)->Evolving<W> = gap.from * filler * gap.to
}

```



### Back Propagation




# Ideas

- use App as a skin only. All processes are to be driven by the evolve-map
- Provide channels for each component

### Logarithmic vs exponential style
- exp(a+b) = exp(a)*exp(b)
- log(a) + log(b) = log(a*b)