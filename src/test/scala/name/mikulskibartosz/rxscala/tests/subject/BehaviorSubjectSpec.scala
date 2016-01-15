package name.mikulskibartosz.rxscala.tests.subject

import name.mikulskibartosz.rxscala.tests.assert.Assertions._
import org.scalatest.{FlatSpec, Matchers}
import rx.lang.scala.subjects.BehaviorSubject
import rx.lang.scala.{Observable, Observer, Subscription}

class BehaviorSubjectSpec extends FlatSpec with Matchers {
  "A behavior subject" should "emit the most recent item and all subsequent items" in {
    var observer: Observer[Int] = null
    val observable = Observable.create[Int](o => {
      observer = o //do not do it at home ;)
      o.onNext(1)
      o.onNext(2)
      Subscription()
    })

    when {
      val subject = BehaviorSubject[Int]()
      observable.subscribe(subject)
      subject
    } assert {
      subscriber => {
        observer.onNext(3)
        observer.onNext(4)
        subscriber.assertValues(2, 3, 4)
      }
    }
  }

  it should "ignore values and emit a failure if the observable has failed" in {
    val observable = Observable.create[Int](o => {
      o.onNext(1)
      o.onNext(2)
      o.onError(new IllegalStateException())
      Subscription()
    })

    when {
      val subject = BehaviorSubject[Int]()
      observable.subscribe(subject)
      subject
    } assert {
      subscriber => {
        subscriber.assertNoValues()
        subscriber.assertError(classOf[IllegalStateException])
      }
    }
  }
}
