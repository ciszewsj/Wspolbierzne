object Generator extends App {

  val size = 1000

  val path = ".\\resources\\"

  val A = Array.ofDim[Double](size, size)
  val r = scala.util.Random
  for (i <- 0 until size) {
    var rowSum = 0.0
    for (j <- 0 until size) {
      if (i != j) {
        A(i)(j) = r.nextDouble()
        rowSum += math.abs(A(i)(j))
      }
    }
    A(i)(i) = rowSum + r.nextDouble() * 10.0
  }
  val writer = new WriterFile("%sA%d.txt".format(path, size))

  writer.saveMatrixToFile(A)

  val Y = new Array[Double](size)
  for (i <- Y.indices) {
    Y(i) = r.nextDouble()
  }
  val writer2 = new WriterFile("%sY%d.txt".format(path, size))

  writer2.saveMatrixToFile(Y)


}
