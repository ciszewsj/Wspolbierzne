import java.io.{File, PrintWriter}

class WriterFile(filePath: String) {

  def saveMatrixToFile(matrix: Array[Double]): Unit = {
    val pw = new PrintWriter(new File(filePath))
    pw.println(matrix.length + " " + 1)

    pw.println(matrix.mkString(" "))
    pw.close()
  }

}

object MainFileWriter extends App {

  def matrix: Array[Double] = Array(
    1,
    2,
    3
  )

  val writer = new WriterFile(".\\resources\\result.txt")
  writer.saveMatrixToFile(matrix)
  sys.exit()
}
