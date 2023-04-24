object Gauss extends App {

  def printArray(arr: Array[Array[Double]]): Unit = {
    for (i <- arr.indices) {
      for (j <- arr(i).indices) {
        print(arr(i)(j) + " ")
      }
      println()
    }
  }

  def printArray(arr: Array[Double]): Unit = {
    for (i <- arr.indices) {
      println(arr(i))
    }
  }

  def gaussJordan(A: Array[Array[Double]], b: Array[Double]): Array[Double] = {
    val n = A.length
    val Ab = Array.tabulate(n) { i => A(i) :+ b(i) }
    for (i <- 0 until n) {
      var maxRow = i
      for (j <- i + 1 until n) {
        if (math.abs(Ab(j)(i)) > math.abs(Ab(maxRow)(i))) {
          maxRow = j
        }
      }
      val tmp = Ab(i); Ab(i) = Ab(maxRow); Ab(maxRow) = tmp
      for (j <- 0 until n) {
        if (j != i) {
          val factor = Ab(j)(i) / Ab(i)(i)
          for (k <- i until n + 1) {
            Ab(j)(k) -= factor * Ab(i)(k)
          }
        }
      }
    }
    val x = Array.fill(n)(0.0)
    for (i <- 0 until n) {
      x(i) = Ab(i)(n) / Ab(i)(i)
    }
    x
  }


  val a = Array(
    Array(1.0, 4.0, 8.0),
    Array(-2.0, 10.0, 16.0),
    Array(3.0, 11.0, 17.0)
  )
  val y = Array(1.0, 2.0, 3.0)

  val x = gaussJordan(a, y)
  printArray(x)
  sys.exit()
}
