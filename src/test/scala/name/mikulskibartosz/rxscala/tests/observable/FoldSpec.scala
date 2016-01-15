package name.mikulskibartosz.rxscala.tests.observable

import name.mikulskibartosz.rxscala.tests.assert.Assertions._
import org.scalatest.{Matchers, FlatSpec}
import rx.lang.scala.Observable

class FoldSpec extends FlatSpec with Matchers {
  val sum: (Int, Int) => Int = _ + _

  "An observable" should "emit a sequence of sums including the initial value" in {
    val observable = Observable.from(Range(0, 4))

    when {
      observable.scan[Int](0)(sum)
    } assert {
      //initial value -> 0
      //(0) + 0
      //(0) + 1
      //(0 + 1) + 2
      //(0 + 1 + 2) + 3
      _.assertValues(0, 0, 1, 3, 6)
    }
  }

  it should "emit a sequence of sums" in {
    val observable = Observable.from(Range(0, 4))

    when {
      observable.scan[Int](sum)
    } assert {
      //first value -> 0
      //(0) + 1
      //(0 + 1) + 2
      //(0 + 1 + 2) + 3
      _.assertValues(0, 1, 3, 6)
    }
  }

  it should "not emit the first and only value if there is only one element" in {
    val observable = Observable.just[Int](1)

    when {
      observable.scan[Int](sum)
    } assert {
      _.assertValue(1)
    }
  }

  it should "emit the sum of an integer sequence" in {
    val observable = Observable.from(Range(0, 20))

    when {
      observable.scan[Int](0)(sum).last
    } assert {
      _.assertValue(190)
    }
  }

  it should "produce an accumulated value of a different type" in {
    val observable = Observable.from(Range(0, 3))

    when {
      val acc: (String, Int) => String = (a, b) => a + b
      observable.scan[String]("")(acc)
    } assert {
      _.assertValues("", "0", "01", "012")
    }
  }
}
