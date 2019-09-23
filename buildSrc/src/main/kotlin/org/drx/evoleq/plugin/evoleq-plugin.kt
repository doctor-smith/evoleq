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

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

val basePath= "/src/generated/kotlin/org/drx/generated"

class EvoleqPlugin: Plugin<Project> {
    override fun apply(target: Project) {

        val dir = File("${target.projectDir}$basePath")
        if(!dir.exists()) {
            dir.mkdirs()
        }
        target.tasks.create("generateSumType", GenerateSumType::class.java)
        target.tasks.create("generateSumTypes", GenerateSumTypes::class.java)
        target.tasks.create("generateProductType", GenerateProductType::class.java)
        target.tasks.create("generateProductTypes", GenerateProductTypes::class.java)
        target.tasks.create("generateKeys", GenerateKeys::class.java)
    }
}



