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
package org.drx.evoleq.dsl

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.coroutines.blockUntil
import org.drx.evoleq.util.intProperty
import org.junit.Test

class ArrayListDslTest {

    @Test fun basics() {
        val list = arrayList<String> {
            item("one")
            item("two")
            item("three")
        }

        assert(list.size == 3)
    }

    @Test fun onNext() = runBlocking {
        val list = arrayListOf<String>()
        var expected: String? = null

        // Case: list is empty
        parallel{
            list.onNext {next ->  expected = next }
        }
        list.add("NEXT")
        delay(500)
        assert(expected == "NEXT")

        // Case: list in non-empty
        expected = null
        list.add("NEXT")
        list.add("NEXT")
        parallel{
            list.onNext {next ->  expected = next }
        }
        delay(500)
        assert(expected == "NEXT")
        parallel{
            list.onNext {next ->  expected = next }
        }
        delay(500)
        assert(expected == "NEXT")
    }

    @Test fun examineObservableList() = runBlocking {
        val list = arrayListOf<Int>()
        val oList = FXCollections.observableList(list)
        list .add(1)
        assert(oList.isNotEmpty())
        list.add(2)
        assert(oList.size == 2)

        val listener = ListChangeListener<Int> { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    println(list.size)
                }
            }
        }
        oList.addListener(listener)
        oList.add(3)
        list.add(6)
        delay(1000)
    }

    @Test fun smartArrayList() = runBlocking {
        val list = smartArrayListOf<Int>()

        parallel{
            delay(1_000)
            list.add(1)
        }
        var time = System.currentTimeMillis()
        blockUntil(list.isEmpty) { value -> value == false}
        time = System.currentTimeMillis() -time
        assert(time > 1_000)

        list.removeAt(0)
        assert(list.isEmpty())

        list.add(1)
        time = System.currentTimeMillis()
        blockUntil(list.isEmpty) { value -> value == false}
        time = System.currentTimeMillis() -time
        assert(time < 2)
        println(time)
    }

    @Test fun onNextOnSmartArrayList() = runBlocking {
        val list = smartArrayListOf<Int>()
        var next:Int = 0
        parallel{
            delay(1_000)
            list.add(1)
        }
        parallel{
            list.onNext { next = it }
        }
        delay(1_100)
        assert(next == 1)
        assert(list.isEmpty.value)
        assert(list.isEmpty())

        parallel{
            delay(1_000)
            list.addAll(arrayListOf(1,2,3,4,5))
        }
        parallel{
            for(i in IntRange(1,5)){
                list.onNext { assert(it == i) }
            }
        }
        delay(1_100)
    }

    @Test fun cancelOnNextOnSmartArrayList() = runBlocking {
        val list = smartArrayListOf<Int>()
        var next:Int = 0
        parallel{
            delay(1_000)
            list.add(1)
        }
        val intProp = intProperty()
        parallel{
            list.onNext(intProp) {
                assert(it == -1)
                it
            }
        }
        parallel{
            intProp.value = -1
            println(intProp.value)
        }
        delay(2_000)
    }
}