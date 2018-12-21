/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.math


infix fun <R,S,T> ((R)->S).then(f:(S)->T): (R)->T = {r -> f(this(r))}
infix fun <R,S,T> ((R)->S).then(f:suspend (S)->T): suspend (R)->T = {r -> f(this(r))}
infix fun <R,S,T> (suspend (R)->S).then(f:(S)->T): suspend (R)->T = {r -> f(this(r))}
infix fun <R,S,T> (suspend(R)->S).then(f: suspend (S)->T): suspend (R)->T = {r -> f(this(r))}


infix fun <R,S,T> ((S)->T).after(f:(R)->S): (R)->T = {r -> this(f(r))}
infix fun <R,S,T> ((S)->T).after(f:suspend(R)->S): suspend (R)->T = {r -> this(f(r))}
infix fun <R,S,T> (suspend(S)->T).after(f:(R)->S): suspend (R)->T = {r -> this(f(r))}
infix fun <R,S,T> (suspend(S)->T).after(f:suspend(R)->S): suspend (R)->T = {r -> this(f(r))}