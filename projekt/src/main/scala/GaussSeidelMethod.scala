import MainWithForLoopNavigation.printArray
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.math.abs

class GaussSeidelMethod extends Solver {
  val ITERATION_LIMIT = 100
  val ERROR = 0.000001

  private def approxEqual(x: Double, y: Double, tolerance: Double): Boolean = {
    val diff = (x - y).abs
    if (x == y) true
    else if (x == 0 || y == 0 || diff < Double.MinValue) diff < tolerance * Double.MinValue
    else diff / (x.abs + y.abs) < tolerance
  }


  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {
    var x: Array[Double] = Array.fill(Y.length)(0.0)
    var itCount = 0
    while (itCount < ITERATION_LIMIT) {

      val xNew = Array.fill(x.length)(0.0)
      for (i <- A.indices) {
        val s1 = (0 until i).map(j => A(i)(j) * xNew(j)).sum
        val s2 = ((i + 1) until A(i).length).map(j => A(i)(j) * x(j)).sum
        xNew(i) = (Y(i) - s1 - s2) / A(i)(i)
      }

      if (x.corresponds(xNew)(approxEqual(_, _, ERROR))) {
        itCount = ITERATION_LIMIT
      }
      else {
        x = xNew
        itCount += 1
      }
    }
    x
  }
}

object GaussSeidelMethodMain extends App {
  def printArray(arr: Array[Double]): String = {
    arr.mkString(", ")
  }

  val aFileName: String = if (args.length >= 1) args(0) else ".\\resources\\A.txt"
  val yFileName: String = if (args.length >= 2) args(1) else ".\\resources\\Y.txt"
  val resultFileName: String = if (args.length >= 3) args(2) else ".\\resources\\result.txt"
  val timeoutTime = if (args.length >= 4) args(3).toInt.seconds else 5.seconds
  val eps: Double = if (args.length >= 5) args(4).toDouble else 0.0001
  println("ARGS ARGUMENTS:")
  println("    1) - aFile path = " + aFileName)
  println("    2) - yFile path = " + yFileName)
  println("    3) - yFile path = " + resultFileName)
  println("    4) - max wait seconds = " + timeoutTime)
  println("    5) - eps = " + eps)
  println()
  implicit val timeout: Timeout = timeoutTime
  val reader = new ReadFile(aFileName, yFileName)
  val matrix = reader.getMatrixFromFiles
  for (i <- 0 until matrix.length - 1) {
    for (j <- i until matrix.length) {
      if (abs(matrix(i)(i)) < abs(matrix(j)(i))) {
        val tmp = matrix(i)
        matrix(i) = matrix(j)
        matrix(j) = tmp
      }
    }
  }
  val A = Array.ofDim[Double](3, 3)
  for (i <- matrix.indices; j <- 0 until 3) {
    A(i)(j) = matrix(i)(j)
  }
  val Y = new Array[Double](matrix.length)
  for (i <- matrix.indices) {
    Y(i) = matrix(i)(matrix.length)
  }

  for (i <- A.indices) {
    println(printArray(A(i)), Y(i))
  }
  val X = new GaussSeidelMethod().solve(A, Y)
  println(printArray(X))
}