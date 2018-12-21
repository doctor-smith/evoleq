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
package org.drx.evoleq.examples.application.fx

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node

fun<N : Node> N.draggable(
    onDragDetectedAction: N.()->Unit,
    onDragAction: N.()->Unit = {},
    onReleaseAction: N.()->Unit = {}
): SimpleBooleanProperty {
    val dragging: SimpleBooleanProperty=  SimpleBooleanProperty(false)
    class Delta(val dX: Double, val dY: Double)
    var x: Double = 0.0
    var y: Double = 0.0
    setOnMousePressed {
        x = it.screenX
        y = it.screenY
    }
    setOnDragDetected {
        dragging.value = true
        onDragDetectedAction()
    }
    setOnMouseDragged {

        translateX += it.screenX - x
        translateY += it.screenY - y

        x = it.screenX
        y = it.screenY

        it.consume()

        onDragAction()
    }
    setOnMouseReleased {
        dragging.value = false
        onReleaseAction()
        it.consume()
        x = 0.0
        y = 0.0
    }
    return dragging
}