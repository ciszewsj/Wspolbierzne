import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Calculate(numbers: List[Int])

class ParallelCalculator extends Actor {
  def receive: Receive = {
    case Calculate(numbers) =>
      // przetwarzanie danych
      val result: Int = numbers.map(x => x * x).sum
      println("Result : ", result)
      // zwrócenie wyniku do nadawcy wiadomości
      sender() ! result
  }
}

object Main extends App {
  implicit val timeout: Timeout = 5.seconds
  val system = ActorSystem("ParallelCalculation")

  def printArray(arr: Array[Double]): Unit = {
    for (i <- arr.indices) {
      print(arr(i) + " ")

      println()
    }
  }

  def gaussElimination(A: Array[Array[Double]], y: Array[Double]): Array[Double] = {
    val n = A.length
    val augmentedMatrix = Array.ofDim[Double](n, n + 1)

    // połącz macierz A i wektor y w jedną macierz rozszerzoną
    for (i <- 0 until n) {
      for (j <- 0 until n) {
        augmentedMatrix(i)(j) = A(i)(j)
      }
      augmentedMatrix(i)(n) = y(i)
    }

    // wykonaj eliminację Gaussa
    for (r <- 0 until n) {
      // znajdź wiersz z niezerowym elementem na diagonali
      var nonzeroRow = r
      while (nonzeroRow < n && augmentedMatrix(nonzeroRow)(r) == 0.0) {
        nonzeroRow += 1
      }

      // jeśli nie znaleziono, to macierz jest osobliwa
      if (nonzeroRow == n) {
        throw new IllegalArgumentException("Macierz osobliwa!")
      }

      // zamień wiersze, jeśli potrzeba
      if (nonzeroRow != r) {
        val tmp = augmentedMatrix(nonzeroRow)
        augmentedMatrix(nonzeroRow) = augmentedMatrix(r)
        augmentedMatrix(r) = tmp
      }

      // wykonaj eliminację dla wierszy o indeksach większych niż r
      for (r2 <- r + 1 until n) {
        val factor = augmentedMatrix(r2)(r) / augmentedMatrix(r)(r)
        for (c <- r until n + 1) {
          augmentedMatrix(r2)(c) -= factor * augmentedMatrix(r)(c)
        }
      }
    }

    // wyznacz wyniki za pomocą metod wstecznej substytucji
    val x = Array.ofDim[Double](n)
    for (r <- n - 1 to 0 by -1) {
      var sum = 0.0
      for (c <- r + 1 until n) {
        sum += augmentedMatrix(r)(c) * x(c)
      }
      x(r) = (augmentedMatrix(r)(n) - sum) / augmentedMatrix(r)(r)
    }

    x
  }

  def gauss(array: Array[Array[Double]], array2: Array[Double]): (Array[Array[Double]], Array[Double]) = {
    for (i <- Array.range(1, array.length)) {
      val k = array(i)(i - 1) / array(i - 1)(i - 1)
      for (h <- Array.range(i, array.length)) {
        for (j <- Array.range(i - 1, array(i).length)) {
          array(h)(j) = array(h)(j) - array(i - 1)(j) * k
        }
        array2(h) = array2(h) - array2(h - 1) * k
      }
    }
    (array, array2)
  }


  def calculateSumAndProduct(a: Int, b: Int): (Int, Int) = {
    val sum = a + b
    val product = a * b
    (sum, product)
  }
  // READ FILES & VALIDATE

  val A = Array(
    Array(1.0, 2.0, 3.0),
    Array(5.0, 6.0, 7.0),
    Array(9.0, 10.0, 12.0)
  )
  val B = Array(4.0, 8.0, 12.0)

  // GAUSS & CALCULATE
  val sum = gaussElimination(A, B)

  printArray(sum)

  sys.exit()
}
