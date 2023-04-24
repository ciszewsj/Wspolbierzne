import scala.io.Source


class ReadFile(aFileName: String, yFileName: String) {

  def getMatrixFromFiles: Array[Array[Double]] = {
    var matrix = readMatrixFromFile(aFileName)
    if (matrix.length != matrix(0).length) {
      throw new IllegalStateException("Wrong matrix A size")
    }
    val matrix2 = readMatrixFromFile(yFileName)
    if (matrix.length != matrix2.length && matrix2(0).length != 1) {
      throw new IllegalStateException("Wrong matrix B size")
    }

    matrix = matrix.zip(matrix2).map { case (row1, row2) => row1 :+ row2(0) }
    matrix
  }

  private def readMatrixFromFile(fileName: String): Array[Array[Double]] = {
    val file = Source.fromFile(fileName)
    val lines = file.getLines.toArray.flatMap(_.split("\t")).flatMap(_.split(" ")).flatMap(_.split("\n"))
    val m = lines(0).toInt
    val n = lines(1).toInt
    if (lines.length != m * n + 2) {
      throw new IllegalStateException("Wrong file length")
    }
    val matrix: Array[Array[Double]] = Array.ofDim[Double](m, n)
    for (i <- Range(0, m)) {
      for (j <- Range(0, n)) {
        matrix(i)(j) = lines(i * n + j + 2).toDouble
      }
    }
    matrix
  }

}

object MainFileReader extends App {

  def printMatrix(matrix: Array[Array[Double]]): Unit = {
    matrix.foreach(row => {
      row.foreach(cell => print(s"$cell\t"))
      println()
    })
  }

  val reader = new ReadFile(".\\resources\\A.txt", ".\\resources\\Y.txt")
  val matrix = reader.getMatrixFromFiles
  printMatrix(matrix)

  sys.exit()
}
