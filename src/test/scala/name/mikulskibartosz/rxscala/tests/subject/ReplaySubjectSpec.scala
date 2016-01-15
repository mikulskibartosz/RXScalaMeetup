package name.mikulskibartosz.rxscala.tests.subject

import name.mikulskibartosz.rxscala.tests.assert.Assertions._
import org.scalatest.{FlatSpec, Matchers}
import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.{Observable, Observer, Subscription}

class ReplaySubjectSpec extends FlatSpec with Matchers {
  "A replay subject" should "emit all value emitted by its source" in {
    var observer: Observer[Int] = null
    val observable = Observable.create[Int](o => {
      observer = o //do not do it at home ;)
      o.onNext(1)
      o.onNext(2)
      Subscription()
    })

    when {
      val subject = ReplaySubject[Int]()
      observable.subscribe(subject)
      subject
    } assert {
      subscriber => {
        observer.onNext(3)
        observer.onNext(4)
        subscriber.assertValues(1, 2, 3, 4)
      }
    }
  }

  it should "cache a specified number of items emitted before subscribing and emit all added afterwards" in {
    var observer: Observer[Int] = null
    val observable = Observable.create[Int](o => {
      observer = o //do not do it at home ;)
      o.onNext(1)
      o.onNext(2)
      o.onNext(3)
      o.onNext(4)
      Subscription()
    })

    when {
      val subject = ReplaySubject.withSize[Int](3)
      observable.subscribe(subject)
      subject
    } assert {
      subscriber => {
        observer.onNext(5)
        observer.onNext(6)
        subscriber.assertValues(2, 3, 4, 5, 6)
      }
    }
  }
}
