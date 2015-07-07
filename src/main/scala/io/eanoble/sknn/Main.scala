package io.eanoble.sknn

import spire.implicits._
import scalaz.concurrent.Task
import scalaz.stream._

object Main extends App {

  case class Observation(label: String, features: Array[Int])

  def classify(o: Array[Observation], f: Array[Int]): String = {
    o.par.map(ob => (ob.label, dist(f, ob.features))).minBy(_._2)._1
  }

  def dist(xs: Array[Int], ys: Array[Int]): Int = {
    var sum = 0
    cfor(0)(_ < xs.length, _ + 1) { i =>
      sum += (xs(i) - ys(i)) ** 2
    }
    sum
  }

  def checkCorrect(obs: Array[Observation], c: Observation): Int = {
    if (c.label == classify(obs, c.features)) 1
    else 0
  }

  // Getting rid of the label at the top
  val training = io.linesR("training.csv").drop(1)
  val validation = io.linesR("validation.csv").drop(1)

  def parseStream(p: Process[Task, String]): Process[Task, Observation] = {
    p.map { s: String =>
      val fields = s.split(',')
      val label = fields(0)
      val rest = fields.tail.map(Integer.parseInt)
      Observation(label, rest)
    }
  }

  val trainingSet = parseStream(training).runLog.run.toArray
  val validationSet = parseStream(validation)

  val streamingResults = validationSet.map(v => checkCorrect(trainingSet, v)).sum.map(s => (s / 500.0).toString)

  (streamingResults to io.stdOutLines).run.run

}
