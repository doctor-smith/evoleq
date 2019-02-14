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

import org.drx.evoleq.dsl.map
import kotlin.reflect.KClass


val Keys: HashMap<Int, KClass<*>> by lazy{
    map<Int,KClass<*>>{
        0 to Key0::class
        1 to Key1::class
        2 to Key2::class
        3 to Key3::class
        4 to Key4::class
        5 to Key5::class
        6 to Key6::class
        7 to Key7::class
        8 to Key8::class
        9 to Key9::class

        10 to Key10::class
        11 to Key11::class
        12 to Key12::class
        13 to Key13::class
        14 to Key14::class
        15 to Key15::class
        16 to Key16::class
        17 to Key17::class
        18 to Key18::class
        19 to Key19::class

        20 to Key20::class
        21 to Key21::class
        22 to Key22::class
        23 to Key23::class
        24 to Key24::class
        25 to Key25::class
        26 to Key26::class
        27 to Key27::class
        28 to Key28::class
        29 to Key29::class

        30 to Key30::class
        31 to Key31::class
        32 to Key32::class
        33 to Key33::class
        34 to Key34::class
        35 to Key35::class
        36 to Key36::class
        37 to Key37::class
        38 to Key38::class
        39 to Key39::class

        40 to Key40::class
        41 to Key41::class
        42 to Key42::class
        43 to Key43::class
        44 to Key44::class
        45 to Key45::class
        46 to Key46::class
        47 to Key47::class
        48 to Key48::class
        49 to Key49::class
    }
}

class TimeoutKey

class Key0
class Key1
class Key2
class Key3
class Key4
class Key5
class Key6
class Key7
class Key8
class Key9

class Key10
class Key11
class Key12
class Key13
class Key14
class Key15
class Key16
class Key17
class Key18
class Key19

class Key20
class Key21
class Key22
class Key23
class Key24
class Key25
class Key26
class Key27
class Key28
class Key29

class Key30
class Key31
class Key32
class Key33
class Key34
class Key35
class Key36
class Key37
class Key38
class Key39

class Key40
class Key41
class Key42
class Key43
class Key44
class Key45
class Key46
class Key47
class Key48
class Key49