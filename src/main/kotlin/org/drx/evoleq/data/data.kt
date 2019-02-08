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
package org.drx.evoleq.data

import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.configure


/**
 * The data functor
 */
open class Data<out D>(val value: D? = null){
    infix fun <E> map(f: (D)-> E): Data<E> = when(value != null) {
        true ->Data(f(value))
        false -> Data()
    }
}

fun <D> data(value: D): Data<D> = Data(value)

sealed class DataTree<out D>
class EmptyDataTree<D> : DataTree<D>()
data class DataNode<D>(val data: Data<D> = Data(), val children: ArrayList<DataTree<D>>) : DataTree<D>()
data class DataLeaf<D>(val data: Data<D>) : DataTree<D>()

infix fun <D,E> DataTree<D>.map(f:(D)->E): DataTree<E> = when(this) {
    is EmptyDataTree -> EmptyDataTree()
    is DataLeaf -> DataLeaf(data map f)
    is DataNode -> DataNode(data map f, arrayListOf(*children.map { it map f }.toTypedArray()))
}


class DataTreeConfiguration<D> : Configuration<DataTree<D>> {

    var data: Data<D> = Data()
    var children: ArrayList<DataTree<D>>? = null


    override fun configure(): DataTree<D> {
        if(data.value == null && children == null) return EmptyDataTree()
        if(data.value != null && children == null ) return DataLeaf(data)
        return DataNode(data, children!!)
    }

    fun data(value: D) {
        data = Data(value)
    }

    fun child(value: D) = child(dataTree{data(value)})

    fun child(tree: DataTree<D>) {
        if(children == null) {
            children = arrayListOf()
        }
        children!!.add(tree)
    }

}

fun <D> dataTree(configuration: DataTreeConfiguration<D>.()->Unit): DataTree<D> = configure(configuration)
