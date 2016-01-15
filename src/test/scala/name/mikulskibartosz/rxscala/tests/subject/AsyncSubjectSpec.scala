package name.mikulskibartosz.rxscala.tests.subject

import org.scalatest.{FlatSpec, Matchers}
import rx.lang.scala.{Subscription, Observable}
import rx.lang.scala.subjects.AsyncSubject

import name.mikulskibartosz.rxscala.tests.assert.Assertions._

class AsyncSubjectSpec extends FlatSpec with Matchers {
  "An async subject" should "emit the last value emitted by the observable it observes" in {
    val observable = Observable.from(Array(1, 2, 3))

    when {
      val subject = AsyncSubject[Int]()
      observable.subscribe(subject)
      subject
    } assert {
      _.assertValue(3)
    }
  }

  it should "emit a failure if the observable has failed" in {
    val observable = Observable.error(new IllegalStateException())

    when {
      val subject = AsyncSubject[Int]()
      observable.subscribe(subject)
      subject
    } assert {
      _.assertError(classOf[IllegalStateException])
    }
  }

  it should "not emit a value if the observable has not completed" in {
    val observable = Observable.create[Int](o => {
      o.onNext(1)
      o.onNext(2)
      o.onNext(3)
      //NOTE the function does not call the onCompleted method

      Subscription()
    })

    when {
      val subject = AsyncSubject[Int]()
      observable.subscribe(subject)
      subject
    } assert {
      subscriber => {
        subscriber.assertNoValues()
        subscriber.assertNotCompleted()
      }
    }
  }
}
