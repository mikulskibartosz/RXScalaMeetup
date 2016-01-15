package name.mikulskibartosz.rxscala.tests.observable

import java.util.concurrent.TimeUnit

import name.mikulskibartosz.rxscala.tests.assert.Assertions._
import org.scalatest.{FlatSpec, Matchers}
import rx.lang.scala.Observable
import rx.lang.scala.observables.ConnectableObservable
import rx.lang.scala.schedulers.TestScheduler

import scala.concurrent.duration.Duration

class HotObservableSpec extends FlatSpec with Matchers {
  "A hot observable" should "emit values before an observer subscribes to it" in {
    val scheduler = TestScheduler()
    val duration = Duration(1, TimeUnit.SECONDS)

    val coldObservable: Observable[Long] =
      Observable.interval(duration, scheduler)
    val hotObservable: ConnectableObservable[Long] =
      coldObservable.publish
    hotObservable.connect

    scheduler.advanceTimeBy(Duration(3, TimeUnit.SECONDS))
    scheduler.triggerActions()

    when {
      hotObservable
    } assert {
      subscriber => {
        scheduler.advanceTimeBy(Duration(3, TimeUnit.SECONDS))
        scheduler.triggerActions()
        subscriber.assertValues(3, 4, 5)
      }
    }
  }

  it should "emit all values if the replay function has been used" in {
    val scheduler = TestScheduler()
    val duration = Duration(1, TimeUnit.SECONDS)

    val coldObservable = Observable.interval(duration, scheduler)
    val hotObservable: ConnectableObservable[Long]
      = coldObservable.replay(Duration(10, TimeUnit.SECONDS))
    hotObservable.connect

    scheduler.advanceTimeBy(Duration(3, TimeUnit.SECONDS))
    scheduler.triggerActions()

    when {
      hotObservable
    } assert {
      subscriber => {
        scheduler.advanceTimeBy(Duration(3, TimeUnit.SECONDS))
        scheduler.triggerActions()
        subscriber.assertValues(0, 1, 2, 3, 4, 5)
      }
    }
  }

  it should "emit all values if the cache function has been used" in {
    val scheduler = TestScheduler()
    val duration = Duration(1, TimeUnit.SECONDS)

    val coldObservable = Observable.interval(duration, scheduler)
    val cachedObservable: Observable[Long] =
      coldObservable.cache

    //the cached observable starts retrieving and caching data when the first subscriber subscribes to it
    cachedObservable.subscribe()

    scheduler.advanceTimeBy(Duration(3, TimeUnit.SECONDS))
    scheduler.triggerActions()

    when {
      cachedObservable
    } assert {
      subscriber => {
        scheduler.advanceTimeBy(Duration(3, TimeUnit.SECONDS))
        scheduler.triggerActions()
        subscriber.assertValues(0, 1, 2, 3, 4, 5)
      }
    }
  }
}
