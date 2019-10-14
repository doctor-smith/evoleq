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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

/**********************************************************************************************************************
 *
 * Keys
 *
 **********************************************************************************************************************/

open class GenerateKeys : DefaultTask() {
    @Suppress("UnstableApiUsage")
    @set:Option(option = "numberOfKeys", description = "The number of keys to be generated")
    @get:Input
    var numberOfKeys: String = "100"

    @TaskAction
    fun generate() {
        val number = Integer.parseInt(numberOfKeys)

        require(number > 99)

        val dir = File("${project.projectDir}$basePath/keys")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        var keys = license()
        keys += "\n\npackage org.drx.generated.keys\n\n\n"

        keys += generateKeys(number-1)

        val keysFile = File("${project.projectDir}$basePath/keys/keys.kt")
        keysFile.writeText(keys)
        println("Generating $number keys")




    }

    fun generateKeys(number: Int) : String {
        var classRefs = ""
        var classes = ""

        IntRange(0,number).forEach {
            classes += "\nclass Key$it"
            classRefs += "\n    map[$it] = Key$it::class"
        }

        return "import kotlin.reflect.KClass\n" +
                "\n" +
                "\n" +
                "val Keys: HashMap<Int, KClass<*>> by lazy{\n" +
                "    val map: HashMap<Int,KClass<*>> = HashMap()" +
                "\n" +
                classRefs +
                "\n" +
                "\n    map" +
                "\n}\n" +
                classes
    }

}

