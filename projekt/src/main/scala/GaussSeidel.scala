import scala.concurrent.duration.FiniteDuration
import scala.math.abs

class GaussSeidel(val timeoutTime: FiniteDuration, val eps: Double, val iteration: Int, val max_error: Double) extends Solver {

  private def approxEqual(x: Double, y: Double, tolerance: Double): Boolean = {
    val diff = (x - y).abs
    if (x == y) true
    else if (x == 0 || y == 0 || diff < Double.MinValue) diff < tolerance * Double.MinValue
    else diff / (x.abs + y.abs) < tolerance
  }


  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {
    var x: Array[Double] = Array.fill(Y.length)(0.0)
    var itCount = 0

    for (i <- 0 until A.length - 1) {
      for (j <- i until A.length) {
        if (abs(A(i)(i)) < abs(A(j)(i))) {
          val tmp = A(i)
          A(i) = A(j)
          A(j) = tmp
          val tmp2 = Y(i)
          Y(i) = Y(j)
          Y(j) = tmp2
        }
      }
    }

    while (itCount < iteration) {

      val xNew = Array.fill(x.length)(0.0)
      for (i <- A.indices) {
        val s1 = (0 until i).map(j => A(i)(j) * xNew(j)).sum
        val s2 = ((i + 1) until A(i).length).map(j => A(i)(j) * x(j)).sum
        xNew(i) = (Y(i) - s1 - s2) / A(i)(i)
      }

      if (x.corresponds(xNew)(approxEqual(_, _, max_error))) {
        itCount = iteration
      }
      else {
        x = xNew
        itCount += 1
      }
    }
    x
  }
}