package name.mikulskibartosz.rxscala.tests.observable

import java.util.concurrent.TimeUnit

import name.mikulskibartosz.rxscala.tests.assert.Assertions._
import org.scalatest.{FlatSpec, Matchers}
import rx.lang.scala.{Scheduler, Observable}
import rx.lang.scala.schedulers.TestScheduler

import scala.concurrent.duration.Duration

class SlidingWindowSpec extends FlatSpec with Matchers {
  "An observable" should "be transformed to observables returning another observable" in {
    val observable = Observable.from(Range(0, 20))

    when {
      //Observable[Observable[Int]]
      observable.sliding(5, 5) //5 elements, starts a new window after 5 items
    } assert {
      List(Range(0, 5), Range(5, 10), Range(10, 15), Range(15, 20))
    }
  }

  it should "be transformed to observables that emit duplicate values" in {
    val observable = Observable.from(Range(0, 20))

    when {
      observable.sliding(5, 4) //5 elements, starts a new window after 4 items
    } assert {
      List(Range(0, 5), Range(4, 9), Range(8, 13), Range(12, 17), Range(16, 20))
    }
  }

  it should "skip some values" in {
    val observable = Observable.from(Range(0, 20))

    when {
      observable.sliding(5, 10) //5 elements, starts a new window after 10 items
    } assert {
      List(Range(0, 5), Range(10, 15))
    }
  }

  it should "emit an observable every 2 seconds" in {
    val scheduler = TestScheduler()
    val observable = Observable
      .interval(Duration(0, TimeUnit.SECONDS), Duration(1, TimeUnit.SECONDS), scheduler)

    when {
      val duration = Duration(2, TimeUnit.SECONDS)
      observable.sliding(duration, duration, scheduler)
    } assert (
      () => {
        scheduler.advanceTimeBy(Duration(4, TimeUnit.SECONDS))
        scheduler.triggerActions()
      },
      List(List(0, 1), List(2, 3))
    )
  }

  it should "emit an observable every 2-3 seconds because the buffer size is set to 3" in {
    val scheduler = TestScheduler()
    val observable = Observable
      .interval(Duration(0, TimeUnit.SECONDS), Duration(1, TimeUnit.SECONDS), scheduler)

    when {
      val duration = Duration(5, TimeUnit.SECONDS)
      val bufferSize = 3
      observable.sliding(duration, duration, bufferSize, scheduler)
    } assert (
      () => {
        scheduler.advanceTimeBy(Duration(10, TimeUnit.SECONDS))
        scheduler.triggerActions()
      },
      List(List(0, 1, 2), List(3, 4), List(5, 6, 7), List(8, 9))
    )
  }

  it should "return a non-overlapping window" in {
    val observable = Observable.from(Range(0, 20))

    when {
      observable.tumbling(5) // == sliding(5, 5)
    } assert {
      List(Range(0, 5), Range(5, 10), Range(10, 15), Range(15, 20))
    }
  }

  //The debounce function drops all values followed by another value within a given time window.
  //Note that the window resets after each value.
  it should "not emit more than one value within a 3 second time window" in {
    val scheduler = TestScheduler()

    //- During the first window the observable emits 3 values (1, 3, 5) and the debounce function drops 1 and 3.
    //- Then (after more than 3 seconds), two observables emit values,
    // the debounce function drops everything that is followed by another value within a 3 second window,
    // therefore the observable emits 12 (the last value).
    val observable = Observable.from(List(1, 3, 5)).delay(Duration(4, TimeUnit.SECONDS), scheduler)
      .merge(
        Observable.from(List(2, 4, 6)).delay(Duration(8, TimeUnit.SECONDS), scheduler)
      ).merge(
        Observable.from(List(8, 10, 12)).delay(Duration(9, TimeUnit.SECONDS), scheduler)
      )

    when {
      observable.debounce(Duration(3, TimeUnit.SECONDS), scheduler)
    } assert {
      testScheduler => {
        scheduler.advanceTimeBy(Duration(10, TimeUnit.SECONDS))
        scheduler.triggerActions()

        testScheduler.assertValues(5, 12)
      }
    }
  }

  //During the presentation, I made a mistake saying the debounce function creates a sliding window that consists of unique values.
  //The following example shows how to create such a window:
  it should "create a sliding window that does not contain duplicate values" in {
    val scheduler = TestScheduler()

    //I stated that the delay function delays every value, in fact it delays the start of the observable.
    //The following function creates an observable that delays every value:
    def createObservable(scheduler: Scheduler) = {
      val values = Observable.from(List(1, 1, 2, 2, 3, 3, 3, 3))
      val time = Observable.interval(Duration(0, TimeUnit.SECONDS), Duration(1, TimeUnit.SECONDS), scheduler)
      values.zip(time).map(_._1)
    }

    val observable = createObservable(scheduler)

    when {
      val duration = Duration(3, TimeUnit.SECONDS)
      observable.tumbling(duration, scheduler).map(_.distinct).flatten
    } assert {
      testScheduler => {
        scheduler.advanceTimeBy(Duration(10, TimeUnit.SECONDS))
        scheduler.triggerActions()
        //[0s, 3s) -> 1, 1, 2 -> 1, 2
        //[3s, 6s) -> 2, 3, 3 -> 2, 3
        //[6s, 10s) -> 3, 3   -> 3

        testScheduler.assertValues(1, 2, 2, 3, 3)
      }
    }
  }
}
