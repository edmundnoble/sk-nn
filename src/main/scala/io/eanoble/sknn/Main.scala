package io.eanoble.sknn

import spire.implicits._

import scala.language.postfixOps
import scalaz.concurrent.Task
import scalaz.stream._

object Main extends App {

  case class Observation(label: String, features: Array[Int])

  def classify(o: Seq[Observation], f: Array[Int]): String = {
    o.par.reduce[Observation] { (a, b) =>
      val firstDist = dist(f, a.features)
      if (firstDist == 0) {
        a
      } else {
        val secondDist = dist(f, b.features)
        if (firstDist > secondDist) b
        else a
      }
    }.label
  }

  def dist(xs: Array[Int], ys: Array[Int]): Int = {
    var sum = 0
    cfor(0)(_ < xs.length, _ + 1) { i =>
      sum += (xs(i) - ys(i)) ** 2
    }
    sum
  }

  def checkCorrect(obs: Array[Observation], c: Observation): Task[Int] = (Task {
    if (c.label == classify(obs, c.features)) 1
    else 0
  })

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

  println(validationSet.evalMap(v => checkCorrect(trainingSet, v)).runLog.run.sum)

}