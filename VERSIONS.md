## Versions
1.1.1: Block coroutine execution while a property has a certain value
1.1.0: Evolving DSL + Structured concurrency
   * OnDemand<D> : Evolving<D>: block execution is delayed until get-method is called
   * Background flow
   * structured concurrency
   * Lazy evolvings, stubs, flows, etc
   * Breaking changes: 
        * Refactored block in Immediate: Its type is no longer suspending

1.0.3: ReceivingStubs: 
   * Use kotlin channels to define receiving-stub. 
   * Interception with Evolvers

1.0.2: Observing stubs:
   * Integrate initial side-effects in a convenient way
   * On the fly: 
       * Added Async as another implementation of the Evolving interface 

1.0.1: Stubs and their dsl

1.0.0: All basic stuff reviewed and tested

1.0.0-beta: All basic stuff
   * The functions evolve and evolveSuspended 
   * Evolving, Immediate, Parallel
   * Flows 
   * Gaps