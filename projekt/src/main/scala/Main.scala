import akka.util.Timeout

import java.util.Date
import scala.concurrent.duration.DurationInt

object Main extends App {
  def printArray(arr: Array[Double]): String = {
    arr.mkString(", ")
  }

  val aFileName: String = if (args.length >= 1) args(0) else ".\\resources\\A.txt"
  val yFileName: String = if (args.length >= 2) args(1) else ".\\resources\\Y.txt"
  val algorithm: Int = if (args.length >= 3) args(2).toInt else 0
  val akkaContainerSize: Int = if (args.length >= 4) args(3).toInt else 0
  val akka: Int = if (args.length >= 5) args(4).toInt else 0
  val resultFileName: String = if (args.length >= 6) args(5) else ".\\resources\\result.txt"
  val timeoutTime = if (args.length >= 7) args(6).toInt.seconds else 5.seconds
  val eps: Double = if (args.length >= 8) args(7).toDouble else 0.0001
  val iteration: Int = if (args.length >= 9) args(8).toInt else 1000
  println("ARGS ARGUMENTS:")
  println("    1) - aFile path = " + aFileName)
  println("    2) - yFile path = " + yFileName)
  println("    3) - Algorithm 0-GS 1-GJ= " + algorithm)
  println("    4) - Akka container size= " + akkaContainerSize)
  println("    5) - Akka 0-on 1-off= " + akka)
  println("    6) - yFile path = " + resultFileName)
  println("    7) - max wait seconds = " + timeoutTime)
  println("    8) - eps = " + eps)
  println("    9) - iteration (only for GS) = " + iteration)
  println()

  implicit val timeout: Timeout = timeoutTime

  val reader = new ReadFile(aFileName, yFileName)
  val matrix = reader.getMatrixFromFiles

  if (matrix._1.length != matrix._2.length || matrix._1.length != matrix._1(0).length) {
    throw new IllegalArgumentException("Matrix's sizes not comparable")
  }

  val solver: Solver = if (akka != 1) if (algorithm == 0) new GaussSeidelParler(timeoutTime, eps, iteration, akkaContainerSize) else new GaussJordanParler(timeoutTime, eps, akkaContainerSize)
  else if (algorithm == 0) new GaussSeidel(eps, iteration) else new GaussJordan(timeoutTime, eps)
  val time: Long = new Date().getTime
  println("Matrix loaded")
  val result = solver.solve(matrix._1, matrix._2)
  val executedTime = new Date().getTime - time
  println("Result:\n" + printArray(result))
  println(s"Executed in: $executedTime [ms]")
  val writer = new WriterFile(resultFileName)
  writer.saveMatrixToFile(result)
  sys.exit()
}
