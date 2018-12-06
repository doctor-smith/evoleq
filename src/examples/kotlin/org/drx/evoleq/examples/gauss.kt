package org.drx.evoleq.examples

import javafx.application.Application.launch
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.Parallel
import org.drx.evoleq.evolve
import org.drx.evoleq.examples.propabilistic.gaussPolar
import tornadofx.action
import kotlin.math.max

class App: tornadofx.App() {

    override fun start(stage: Stage){
        val pane = Pane()

        val scene = Scene(pane, 1200.0, 400.0)

        val button = Button("new Gaussian sample")
        button.action {

            val mu = 0.0
            val sigma = 0.5

            val numOfSamples = 1000
            val groupSize = 10
            val part =  groupSize.toDouble() / numOfSamples.toDouble()

            val scale = 100.0
            GlobalScope.launch {
            val list = Parallel {evolve<Pair<ArrayList<Double>,Long>,Long>(
                initialData = Pair(arrayListOf<Double>(), 0L),
                conditions = EvolutionConditions<Pair<ArrayList<Double>, Long>, Long>(
                    testObject = 0,
                    check = { index -> index <= numOfSamples },
                    updateCondition = { pair -> pair.second }
                )
            ){
                list ->  Parallel {
                    list.first.add(gaussPolar(mu, sigma))
                    list.copy(second = list.second +1)
                }
            } }.get()

            list.first.sort()



            val groups = arrayListOf<ArrayList<Double>>()
                var cnt: Int = 0
                var tmp: ArrayList<Double> = arrayListOf()
            list.first.forEach {
                 tmp.add(it)
                if(cnt == groupSize-1){
                    groups.add(tmp)
                    tmp = arrayListOf()
                    cnt=0
                }
                else {
                    cnt++
                }
            }
                println(groups.size)
            Platform.runLater {
                val center = 600.0;
                val base = 300.0
                val lambda = scale;
                groups.map {
                    println(it.size)

                    val x = it.first()
                    val width = it.last() - x
                    val height = part / width
                    val y = -height
                    Rectangle(
                        x,y,width,height
                    )
                }.map{val r =Rectangle(
                    lambda * it.x + center,
                    lambda * it.y +base,
                    max(lambda* it.width,1.0) ,
                    lambda * it.height
                    )
                    r.fill = Color.RED
                    println(r.toString())
                    r

                }
                .forEach {
                    pane.children.add(it)
                }
                /*
                list.forEach {
                    pane.children.add(Circle(100 * it + 600.0, 300.0, 1.0, Color.RED))
                }
                */
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