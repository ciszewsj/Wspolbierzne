import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

case class SendNewX(index: Array[Int], y: Array[Double])

case class Start(actors: Array[ActorRef])

case class YieldNewX(y: Array[Double])

case class Stop()

case class Ended()

case class Result()

class RowActorGS(private val index: Array[Int], private val size: Int, private val a: Array[Array[Double]], private val y: Array[Double], max_iteration: Int, eps: Double) extends Actor {
  var x: Array[Double] = Array.fill(size)(0.0)
  var old_x: Array[Double] = x.clone()
  var actors: Array[ActorRef] = new Array[ActorRef](0)
  var end: Boolean = false
  var iteration = 0
  if (a.length != index.length) {
    throw new IllegalArgumentException("Matrix have diff sizes")
  }

  if (index.exists(localIndex => Math.abs(a(index.indexOf(localIndex))(localIndex)) < eps)) {
    throw new IllegalArgumentException("Matrix have 0 on diagonal")
    sys.exit()
  }

  private def approxEqual(x: Double, y: Double, tolerance: Double): Boolean = {
    val diff = (x - y).abs
    if (x == y) true
    else if (x == 0 || y == 0 || diff < Double.MinValue) diff < tolerance * Double.MinValue
    else diff / (x.abs + y.abs) < tolerance
  }

  private def calculate(): Unit = {
    if (!(iteration < max_iteration) || iteration != 0 && old_x.corresponds(x)(approxEqual(_, _, eps))) {
      actors.foreach(actor => actor ! Stop)
    } else {
      old_x = x
      x = Array.fill(size)(0.0)
      index.foreach(localIndex => {
        val itmp = index.indexOf(localIndex)
        val s1: Double = (0 until localIndex).map(j => a(itmp)(j) * old_x(j)).sum
        val s2: Double = (localIndex + 1 until a.length).map(j => a(itmp)(j) * old_x(j)).sum
        val x_new: Double = (y(itmp) - s1 - s2) / a(itmp)(localIndex)
        x(localIndex) = x_new
      })
      if (actors(0) == self && actors.length > 1) {
        actors(1) ! SendNewX(index, index.map(i => x(i)))
      } else if (actors.length == 1) {
        actors(0) ! YieldNewX(index.map(i => x(i)))
      }
      iteration += 1
    }
  }

  override def receive: Receive = {
    case Start(actors) =>
      this.actors = actors
      calculate()

    case SendNewX(index: Array[Int], y: Array[Double]) =>
      if (actors.last != self) {
        actors(actors.indexOf(self) + 1) ! SendNewX(index ++ this.index, y ++ this.index.map(i => x(i)))
      } else {
        index.zip(y).foreach { case (i, value) => x(i) = value }
        actors.foreach(actor => actor ! YieldNewX(x))
      }

    case YieldNewX(x) =>
      this.x = x
      calculate()

    case Stop =>
      end = true

    case Ended =>
      sender() ! this.end

    case Result =>
      sender() ! x
  }
}

class GaussSeidelParler(val timeoutTime: FiniteDuration, val eps: Double, val iteration: Int, chunkSize: Int) extends Solver {

  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {
    var x: Array[Double] = null

    implicit val timeout: Timeout = timeoutTime
    val system = ActorSystem()
    val size = Y.length
    val actor_array = A.indices.map(i => i)
      .toList
      .grouped(chunkSize)
      .map(indexes => {
        system.actorOf(
          Props(
            new RowActorGS(indexes.toArray,
              size,
              indexes.map(index => A(index)).toArray,
              indexes.map(index => Y(index)).toArray,
              iteration,
              eps)
          )
        )
      }).toArray

    val time: Long = new Date().getTime
    actor_array.foreach(actor => actor ! Start(actor_array))

    do {
      if (Await.result((actor_array(0) ? Ended).mapTo[Boolean], timeoutTime)) {
        x = Await.result((actor_array(0) ? Result).mapTo[Array[Double]], timeoutTime)
      }
    } while (x == null)
    val executedTime = new Date().getTime - time
    println(s"Executed Internal time in: $executedTime [ms]")
    x
  }
}