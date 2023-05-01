import scala.util.control.Breaks.break

object GaussSeidel extends App {
  val ITERATION_LIMIT = 1000
  val ERROR = 0.000001

  def approxEqual(x: Double, y: Double, tolerance: Double): Boolean = {
    val diff = (x - y).abs
    if (x == y) true
    else if (x == 0 || y == 0 || diff < Double.MinValue) diff < tolerance * Double.MinValue
    else diff / (x.abs + y.abs) < tolerance
  }

  //  val A: Array[Array[Double]] = Array(
  //    Array(10.0, -1.0, 2.0),
  //    Array(-1.0, 11.0, -1.0),
  //    Array(2.0, -1.0, 10.0)
  //  )
  val A: Array[Array[Double]] = Array(
    Array(7.0, 8.0, 3.0),
    Array(6.0, 12.0, 14.0),

    Array(1.0, 2.0, 3.0),
  )
  val b: Array[Double] = Array(2.0, 4.0, 3.0)

  var x: Array[Double] = Array.fill(b.length)(0.0)
  var itCount = 0
  while (itCount < ITERATION_LIMIT) {

    val xNew = Array.fill(x.length)(0.0)
    for (i <- A.indices) {
      val s1 = (0 until i).map(j => A(i)(j) * xNew(j)).sum
      val s2 = ((i + 1) until A(i).length).map(j => A(i)(j) * x(j)).sum
      xNew(i) = (b(i) - s1 - s2) / A(i)(i)
    }

    if (x.corresponds(xNew)(approxEqual(_, _, ERROR))) {
      itCount = ITERATION_LIMIT
    }
    else {
      x = xNew
      itCount += 1
    }
  }

  println("Solution:")
  println(x.mkString(" "))
  val error = (for (i <- A.indices) yield {
    (for (j <- A(i).indices) yield A(i)(j) * x(j)).sum - b(i)
  }).toArray
  println("Error:")
  println(error.mkString(" "))

}
