import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

case class Subtract(index: Int, row: Array[Double])

case object Divide

case object GetRow

case object GetResult

case class GetRowResponse(rowId: Int, row: Array[Double])

case class IsZeroElement(rowId: Int, eps: Double)

case class ChangeRow(index: Int)

class RowActor(val rowID: Int, var row: Array[Double]) extends Actor {

  var tmpRowId: Int = rowID

  override def receive: Receive = {

    case Subtract(index: Int, array: Array[Double]) =>
      val ratio = row(index) / array(index)
      for (i <- Range(index, row.length)) {
        row(i) -= array(i) * ratio
      }

    case Divide =>
      val ratio = row(tmpRowId)
      row = row.map(x => x / ratio)

    case GetRow => sender() ! GetRowResponse(tmpRowId, row)

    case GetResult => sender() ! row.last

    case IsZeroElement(index: Int, eps: Double) => sender() ! (row(index).abs <= eps)

    case ChangeRow(index: Int) => tmpRowId = index
  }
}

object MainWithForLoopNavigation extends App {
  def printArray(arr: Array[Double]): String = {
    arr.mkString(", ")
  }

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

  val matrix = reader.getMatrixFromFiles

  val system = ActorSystem()
  val actor_array: Array[ActorRef] = matrix.zipWithIndex.map { case (value, index) => system.actorOf(Props(new RowActor(index, value))) }

  for (i <- actor_array.indices) {
    if (Await.result((actor_array(i) ? IsZeroElement(i, eps)).mapTo[Boolean], timeoutTime)) {
      var found = false
      for (j <- Range(i + 1, actor_array.length)) {
        if (!Await.result((actor_array(j) ? IsZeroElement(i, eps)).mapTo[Boolean], timeoutTime)) {
          val tmp = actor_array(i)
          actor_array(i) = actor_array(j)
          actor_array(j) = tmp

          actor_array(i) ! ChangeRow(i)
          actor_array(j) ! ChangeRow(j)
          found = true
        }
      }
      if (!found) {
        println("Matrix have 0 on diagonal")
        sys.exit()
      }
    }
    actor_array(i) ! Divide
    val rowResponse: GetRowResponse = Await.result((actor_array(i) ? GetRow).mapTo[GetRowResponse], timeoutTime)
    for (j <- actor_array.indices) {
      if (i != j) {
        actor_array(j) ! Subtract(rowResponse.rowId, rowResponse.row)
      }
    }
  }

  val result: Array[Double] = Array.fill(actor_array.length)(0)
  for (i <- actor_array.indices) {
    println("ID " + i + " : " + printArray(Await.result((actor_array(i) ? GetRow).mapTo[GetRowResponse], timeoutTime).row))
    result(i) = Await.result((actor_array(i) ? GetResult).mapTo[Double], timeoutTime)
  }
  println()
  println("Result:\n" + printArray(result))
  val writer = new WriterFile(resultFileName)
  writer.saveMatrixToFile(result)
  sys.exit()
}