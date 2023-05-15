class GaussSeidelNormal(val eps: Double, val iteration: Int) extends Solver {
  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {
    val n = A.length
    val x = Array.fill(n)(0.0)

    var maxError = Double.MaxValue
    var iter = 0

    while (maxError > eps && iter < iteration) {
      maxError = 0.0

      for (i <- 0 until n) {
        var sigma = 0.0

        for (j <- 0 until n) {
          if (i != j) {
            sigma += A(i)(j) * x(j)
          }
        }

        val xi = (Y(i) - sigma) / A(i)(i)
        maxError = math.max(maxError, math.abs(x(i) - xi))
        x(i) = xi
      }

      iter += 1
    }

    x

  }
}
