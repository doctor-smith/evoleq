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