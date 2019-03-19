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
package org.drx.evoleq.stub

fun generateKeys(number: Int) : String {
    var classRefs = ""
    var classes = ""


    IntRange(0,number).forEach {
        classes += "\nclass Key$it"
        classRefs += "\n        $it to Key$it::class"
    }

    return "package org.drx.evoleq.stub\n" +
            "\n" +
            "import org.drx.evoleq.dsl.map\n" +
            "import kotlin.reflect.KClass\n" +
            "\n" +
            "\n" +
            "val Keys: HashMap<Int, KClass<*>> by lazy{\n" +
            "    map<Int,KClass<*>>{" +
            classRefs +
            "\n}}\n" +
            classes
}

fun main() {
    println(generateKeys(1000))
}