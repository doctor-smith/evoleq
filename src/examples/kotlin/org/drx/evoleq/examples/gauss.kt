package org.drx.evoleq.examples

import javafx.application.Application.launch
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.drx.evoleq.EvolutionConditions
import org.drx.evoleq.Parallel
import org.drx.evoleq.evolve
import org.drx.evoleq.experimental.gauss
import tornadofx.action

class App: tornadofx.App() {

    override fun start(stage: Stage){
        val pane = Pane()

        val scene = Scene(pane, 1200.0, 400.0)

        val button = Button("new Gaussian sample")
        button.action {
            GlobalScope.launch {
            val list = Parallel {evolve<ArrayList<Double>,Int>(
                initialData = arrayListOf<Double>(),
                conditions = EvolutionConditions<ArrayList<Double>, Int>(
                    testObject =  0,
                    check = { index -> index < 100 },
                    updateCondition = { list -> list.size +1 }
                )
            ){
                list ->  Parallel {
                    list.add(gauss(0.0,1.0))
                    list
                }
            } }.get()

            list.sort()
            val groups = arrayListOf<ArrayList<Double>>()
                var cnt: Int = 0
                var tmp: ArrayList<Double> = arrayListOf()
            list.forEach {
                 tmp.add(it)
                if(cnt == 9){
                    groups.add(tmp)
                    tmp = arrayListOf()
                    cnt=0
                }

            }
            Platform.runLater {
                list.forEach {
                    pane.children.add(Circle(100 * it + 600.0, 300.0, 1.0, Color.RED))
                }
            }
        }}

        pane.children.add( button )

        pane.children.add( Line(100.0, 300.0, 1100.0, 300.0) )


        stage.scene = scene
        stage.show()
    }

}

fun main(args: Array<String>){
    launch(App::class.java)
}