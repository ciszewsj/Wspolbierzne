

class GaussJordan( val eps: Double) extends Solver {
  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {
    val n = A.length
    val matrix = A.zip(Y).map { case (row1, row2) => row1 :+ row2 }

    for (k <- 0 until n) {
      val pivot = matrix(k)(k)
      for (j <- k until n + 1) {
        matrix(k)(j) /= pivot
      }

      for (i <- 0 until n) {
        if (i != k) {
          val factor = matrix(i)(k)
          for (j <- k until n + 1) {
            matrix(i)(j) -= factor * matrix(k)(j)
          }
        }
      }
    }

    val result = Array.ofDim[Double](n)
    for (i <- 0 until n) {
      result(i) = matrix(i)(n)
    }
    result
  }

}