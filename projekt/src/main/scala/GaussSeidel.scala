import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

case class SendNewX(index: Int, y: Double)

case class Start(actors: Array[ActorRef])

case class Stop()

case class Ended()

case class Result()

class RowActorGS(private val index: Int, private val a: Array[Double], private val y: Double, max_iteration: Int, eps: Double) extends Actor {
  var x: Array[Double] = Array.fill(a.length)(0.0)
  var old_x: Array[Double] = x.clone()
  var actors: Array[ActorRef] = new Array[ActorRef](0)
  var received = 0
  var end: Boolean = false
  var iteration = 0

  private def approxEqual(x: Double, y: Double, tolerance: Double): Boolean = {
    val diff = (x - y).abs
    if (x == y) true
    else if (x == 0 || y == 0 || diff < Double.MinValue) diff < tolerance * Double.MinValue
    else diff / (x.abs + y.abs) < tolerance
  }

  private def calculate(): Unit = {
    if (!(iteration < max_iteration) || iteration != 0 && old_x.corresponds(x)(approxEqual(_, _, eps))) {
      actors.foreach(actor => actor ! Stop)
    }
    else {
      old_x = x
      x = Array.fill(a.length)(0.0)
      val s1 = (0 until index).map(j => a(j) * old_x(j)).sum
      val s2 = (index + 1 until a.length).map(j => a(j) * old_x(j)).sum
      val x_new = (y - s1 - s2) / a(index)
      actors.foreach(actor => actor ! SendNewX(index, x_new))
      iteration += 1
    }
  }

  override def receive: Receive = {

    case Start(actors) =>
      this.actors = actors
      calculate()
    case SendNewX(index, y) =>
      received += 1
      x(index) = y
      if (received == a.length) {
        calculate()
        received = 0
      }
    case Stop =>
      end = true
    case Ended =>
      sender() ! this.end
    case Result =>
      sender() ! x
  }
}

class GaussSeidel(val timeoutTime: FiniteDuration, val eps: Double, val iteration: Int) extends Solver {

  override def solve(A: Array[Array[Double]], Y: Array[Double]): Array[Double] = {
    var x: Array[Double] = null

    implicit val timeout: Timeout = timeoutTime
    val system = ActorSystem()
    val actor_array = new Array[ActorRef](A.length)
    for (i <- A.indices) {
      actor_array(i) = system.actorOf(Props(new RowActorGS(i, A(i), Y(i), iteration, eps)))
    }

    A.indices.foreach(i => actor_array(i) ! Start(actor_array))

    do {
      if (Await.result((actor_array(0) ? Ended).mapTo[Boolean], timeoutTime)) {
        x = Await.result((actor_array(0) ? Result).mapTo[Array[Double]], timeoutTime)
      }
    } while (x == null)

    x
  }
}