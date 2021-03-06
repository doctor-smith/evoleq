/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.evoleq.evolving

import kotlinx.coroutines.*
import org.drx.evoleq.dsl.immediate
import org.drx.evoleq.dsl.lazyParallel
import org.drx.evoleq.dsl.parallel
import org.junit.Test

class EvolvingTest {

    @Test fun defineLazyEvolving() = runBlocking{
        val lE: LazyEvolving<Int> = { x -> when(x<0 ){
            true -> immediate{0}
            false -> parallel { x*x }
        }}

        val scope = CoroutineScope(Job())

        assert(scope.lE(0).get() == 0)
    }

    @Test fun lazyEvolvingInheritanceAndCancellation() = runBlocking{
        val lE:LazyEvolving<Int> = lazyParallel { x -> delay(1_000); x }
        val scope = CoroutineScope(Job())

        val res = scope.lE(0)
        val j = res.job
        scope.cancel()
        delay(10)
        assert(j.isCancelled)
    }



}