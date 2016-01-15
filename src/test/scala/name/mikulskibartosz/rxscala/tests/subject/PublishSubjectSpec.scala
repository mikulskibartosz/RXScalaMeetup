package name.mikulskibartosz.rxscala.tests.subject

import name.mikulskibartosz.rxscala.tests.assert.Assertions._
import org.scalatest.{FlatSpec, Matchers}
import rx.lang.scala.subjects.PublishSubject
import rx.lang.scala.{Observable, Observer, Subscription}

class PublishSubjectSpec extends FlatSpec with Matchers {
  "A publish subject" should "emit all values emitted by the source after the subject subscribed to it" in {
    var observer: Observer[Int] = null
    val observable = Observable.create[Int](o => {
      observer = o //do not do it at home ;)
      o.onNext(1)
      o.onNext(2)
      Subscription()
    })

    when {
      val subject = PublishSubject[Int]()
      observable.subscribe(subject)
      subject
    } assert {
      subscriber => {
        observer.onNext(3)
        observer.onNext(4)
        subscriber.assertValues(3, 4)
      }
    }
  }

  //NOTE every subject can subscribe to more than one observable, but this one does not do anything interesting, therefore it is easy to explain its behaviour
  it should "emit values from multiple sources" in {
    var firstObserver: Observer[Int] = null
    val firstObservable = Observable.create[Int](o => {
      firstObserver = o
      Subscription()
    })

    var secondObserver: Observer[Int] = null
    val secondObservable = Observable.create[Int](o => {
      secondObserver = o
      Subscription()
    })

    when {
      val subject = PublishSubject[Int]()
      firstObservable.subscribe(subject)
      secondObservable.subscribe(subject)
      subject
    } assert {
      subscriber => {
        firstObserver.onNext(1)
        secondObserver.onNext(2)
        subscriber.assertValues(1, 2)
      }
    }
  }
}
