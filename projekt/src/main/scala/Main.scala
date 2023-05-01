import akka.util.Timeout

import scala.concurrent.duration.DurationInt

object Main extends App {
  val aFileName: String = if (args.length >= 1) args(0) else ".\\resources\\A.txt"
  val yFileName: String = if (args.length >= 2) args(1) else ".\\resources\\Y.txt"
  val resultFileName: String = if (args.length >= 3) args(2) else ".\\resources\\result.txt"
  val timeoutTime = if (args.length >= 4) args(3).toInt.seconds else 5.seconds
  val eps: Double = if (args.length >= 5) args(4).toDouble else 0.0001
  println("ARGS ARGUMENTS:")
  println("    1) - aFile path = " + aFileName)
  println("    2) - yFile path = " + yFileName)
  println("    3) - yFile path = " + resultFileName)
  println("    4) - max wait seconds = " + timeoutTime)
  println("    5) - eps = " + eps)
  println()
  implicit val timeout: Timeout = timeoutTime
  val reader = new ReadFile(aFileName, yFileName)
}
