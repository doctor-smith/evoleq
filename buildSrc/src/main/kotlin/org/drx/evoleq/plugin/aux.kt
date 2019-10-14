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

package org.drx.evoleq.plugin
/**********************************************************************************************************************
 *
 * Auxiliary functions
 *
 **********************************************************************************************************************/

public fun license(): String {
    val obj = object {}
    val license = obj::class.java.getResource("LICENSE").readText().replace("\n", "\n * ")
    return "/**\n * $license\n */"
}

public fun dist() = "\n\n"

public fun buildGenericTypes(dimension: Int, type: String, variance: String? = null): String{
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach { list.add(0,"${if(variance != null){"$variance "}else{""}}$type$it") }
    return list.joinToString ( ", " )
}

public fun buildGenericTypes(dimension: Int, type: String, index: Int, typeAtIndex: String, variance: String? = null): String{
    var result = ""
    val list = arrayListOf<String>()
    IntRange(1,dimension).forEach {
        val string  = if(it == index){"$typeAtIndex$it"}else{"$type$it"}
        list.add(0,"${if(variance != null){"$variance "}else{""}}$string")
    }
    return list.joinToString ( ", " )
}

public fun buildIdLambda(type: String) = "{ ${type.toLowerCase()} : ${type.toUpperCase()} -> ${type.toLowerCase()} }"