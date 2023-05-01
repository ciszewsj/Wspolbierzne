import Main.printArray
import akka.actor.Actor

import scala.concurrent.duration.FiniteDuration

class RowActorGS(val rowID: Int, var row: Array[Double]) extends Actor {

  var tmpRowId: Int = rowID

  override def receive: Receive = {

    case Subtract(index: Int, array: Array[Double]) =>
      val ratio = row(index) / array(index)
      for (i <- Range(index, row.length)) {
        row(i) -= array(i) * ratio
      }

    case Divide =>
      val ratio = row(tmpRowId)
      row = row.map(x => x / ratio)

    case GetRow => sender() ! GetRowResponse(tmpRowId, row)

    case GetResult => sender() ! row.last

    case IsZeroElement(index: Int, eps: Double) => sender() ! (row(index).abs <= eps)

    case ChangeRow(index: Int) => tmpRowId = index
  }
}

class GaussSeidel(val timeoutTime: FiniteDuration, val eps: Double, val iteration: Int) extends Solver {

  private def approxEqual(x: Double, y: Double, tolerance: Double): Boolean = {
    val diff = (x - y).abs
    if (x == y) true
    else if (x == 0 || y == 0 || diff < Double.MinValue) diff < tolerance * Double.MinValue
    else diff / (x.abs + y.abs) < tolerance
  }

  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {
    var x: Array[Double] = Array.fill(Y.length)(0.0)
    var itCount = 0

    for (x <- A.indices) {
      println(printArray(A(x)))
    }
    println(printArray(Y))
    println(A.length, A(0).length, Y.length)
    while (itCount < iteration) {
      val xNew = Array.fill(x.length)(0.0)
      for (i <- A.indices) {
        val s1 = (0 until i).map(j => A(i)(j) * xNew(j)).sum
        val s2 = ((i + 1) until A(i).length).map(j => A(i)(j) * x(j)).sum
        xNew(i) = (Y(i) - s1 - s2) / A(i)(i)
      }
      if (x.corresponds(xNew)(approxEqual(_, _, eps))) {
        itCount = iteration
      }
      else {
        x = xNew.clone()
        itCount += 1
      }
    }

    x
  }
}