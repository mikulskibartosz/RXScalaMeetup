package name.mikulskibartosz.rxscala.tests.assert

import org.scalatest.Matchers._
import rx.lang.scala.Observable
import rx.lang.scala.observers.TestSubscriber


case class AssertSingle[T](objectUnderTest: rx.Single[T]) {
  def assert(assert: rx.observers.TestSubscriber[T] => Unit) = {
    val subscriber = new rx.observers.TestSubscriber[T]()

    objectUnderTest.subscribe(subscriber)
    assert(subscriber)
  }
}

case class AssertJavaObservable[T](objectUnderTest: rx.Observable[T]) {
  def assert(assert: rx.observers.TestSubscriber[T] => Unit) = {
    val subscriber = new rx.observers.TestSubscriber[T]()

    objectUnderTest.subscribe(subscriber)
    assert(subscriber)

  }
}

case class AssertScalaObservable[T](objectUnderTest: Observable[T]) {
  def assert(assert: TestSubscriber[T] => Unit) = {
    val subscriber = TestSubscriber[T]()

    objectUnderTest.subscribe(subscriber)
    assert(subscriber)

  }
}

case class AssertNestedScalaObservable[T](objectUnderTest: Observable[Observable[T]]) {
  def assert(expectedValues: Seq[Iterable[T]]): Unit = assert(() => (), expectedValues)

  def assert(triggerActions: () => Unit, expectedValues: Seq[Iterable[T]]): Unit = {
    val outerSubscriber = TestSubscriber[Observable[T]]()

    objectUnderTest.subscribe(outerSubscriber)
    triggerActions()

    val innerObservables = outerSubscriber.getOnNextEvents

    for((expected, observable) <- expectedValues.zip(innerObservables)) {
      val testSubscriber = rx.lang.scala.observers.TestSubscriber[T]()
      observable.subscribe(testSubscriber)

      val values = testSubscriber.getOnNextEvents

      values should contain theSameElementsInOrderAs expected
    }
  }
}

object Assertions {
  def when[T](createObjectUnderTest: => rx.Single[T]): AssertSingle[T] = new AssertSingle[T](createObjectUnderTest)

  def when[T](createObjectUnderTest: => rx.Observable[T]): AssertJavaObservable[T] = new AssertJavaObservable[T](createObjectUnderTest)

  def when[T](createObjectUnderTest: => Observable[T]): AssertScalaObservable[T] = new AssertScalaObservable[T](createObjectUnderTest)

  def when[T](createObjectUnderTest: => Observable[Observable[T]]): AssertNestedScalaObservable[T] = new AssertNestedScalaObservable[T](createObjectUnderTest)
}
