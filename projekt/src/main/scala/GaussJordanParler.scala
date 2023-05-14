import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

case class Subtract(index: Int, row: Array[Double])

case class Divide(index: Int)

case class GetRow(index: Int)

case object GetResult

case class GetRowResponse(rowId: Int, row: Array[Double])

case class GetMaxInMatrix(position: Int)

case class MaxInMatrix(index: Int, double: Double, ref: ActorRef)

case class SetRow(index: Int, array: Array[Double])

class RowActorGJ(val indexes: Array[Int], var row: Array[Array[Double]]) extends Actor {
  override def receive: Receive = {

    case Subtract(index: Int, array: Array[Double]) =>
      row.filter(r => indexes(row.indexOf(r)) != index
      ).foreach(r => {
        val ratio = r(index) / array(index)
        for (i <- Range(index, r.length)) {
          r(i) -= array(i) * ratio
        }
      })


    case Divide(global_index) =>
      val index = indexes.indexOf(global_index)
      val ratio: Double = row(index)(global_index)
      row(index) = row(index).map(x => x / ratio)


    case GetRow(global_index) =>
      val index = indexes.indexOf(global_index)
      sender() ! GetRowResponse(global_index, row(index))

    case GetResult => sender() ! row.map(r => r.last)

    case GetMaxInMatrix(position: Int) =>
      val index: Int = indexes.maxBy(index =>
        Math.abs(row(indexes.indexOf(index))(position))
      )
      sender() ! MaxInMatrix(index, row(indexes.indexOf(index))(position), self)

    case SetRow(index: Int, array: Array[Double]) =>
      row(indexes.indexOf(index)) = array

  }
}


class GaussJordanParler(val timeoutTime: FiniteDuration, val eps: Double, chunkSize: Int) extends Solver {
  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {

    val matrix = A.zip(Y).map { case (row1, row2) => row1 :+ row2 }
    implicit val timeout: Timeout = timeoutTime
    val system = ActorSystem()
    val indexes = A.indices.map(i => i).toList.grouped(chunkSize).toList
    val actor_array = indexes
      .map(indexes => {
        system.actorOf(
          Props(
            new RowActorGJ(indexes.toArray,
              indexes.map(index => matrix(index)).toArray)
          )
        )
      }).toArray

    if (indexes.isEmpty) {
      throw new IllegalArgumentException("null exception")
    }

    for (index <- indexes) {
      val commId = indexes.indexOf(index)
      for (i <- index) {
        val topIndex = actor_array.map(actor =>
          Await.result((actor ? GetMaxInMatrix(i)).mapTo[MaxInMatrix], timeoutTime)
        ).maxBy(elem => elem.double)
        if (topIndex.index != i) {
          val tmp = Await.result((actor_array(commId) ? GetRow(i)).mapTo[GetRowResponse], timeoutTime).row
          val tmp2 = Await.result((topIndex.ref ? GetRow(topIndex.index)).mapTo[GetRowResponse], timeoutTime).row

                    actor_array(commId) ! SetRow(i, tmp2)
                    topIndex.ref ! SetRow(topIndex.index, tmp)
        }

        actor_array(commId) ! Divide(i)
        val rowResponse: GetRowResponse = Await.result((actor_array(commId) ? GetRow(i)).mapTo[GetRowResponse], timeoutTime)


        for (j <- actor_array.indices) {
          actor_array(j) ! Subtract(rowResponse.rowId, rowResponse.row)
        }
      }
    }
    val result: Array[Double] = actor_array.flatMap(actor => Await.result((actor ? GetResult).mapTo[Array[Double]], timeoutTime))
    result
  }
}