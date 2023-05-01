import java.io.{File, PrintWriter}

class WriterFile(filePath: String) {
  def saveMatrixToFile(matrix: Array[Double]): Unit = {
    val pw = new PrintWriter(new File(filePath))
    pw.println(matrix.length + " " + 1)

    pw.println(matrix.mkString(" "))
    pw.close()
  }

  def saveMatrixToFile(matrix: Array[Array[Double]]): Unit = {
    val pw = new PrintWriter(new File(filePath))
    pw.println(matrix.length + " " + matrix(0).length)
    matrix.foreach(row => pw.println(row.mkString(" ")))
    pw.close()
  }
}
