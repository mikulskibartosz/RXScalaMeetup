package name.mikulskibartosz.rxscala.tests.single

import java.util.concurrent.{Callable, TimeUnit}

import com.google.common.util.concurrent.MoreExecutors
import name.mikulskibartosz.rxscala.tests.assert.Assertions._
import org.scalatest.{FlatSpec, Matchers}

//NOTE that the single observable is not in the rx.lang.scala package
import rx.Single
import rx.functions.{Func1, Func2}
import rx.schedulers.Schedulers

class SingleObservableSpec extends FlatSpec with Matchers {

  val executor = MoreExecutors.newDirectExecutorService()

  "A single observable" should "return one value" in {
    val text = "It is not a very useful test, isn't it?"

    when {
      Single.just(text)
    } assert {
      _.assertValue(text)
    }
  }

  it should "return an exception" in {
    val expectedExceptionClass = classOf[IllegalStateException]

    when {
      Single.error[String](new IllegalStateException())
    } assert {
      _.assertError(expectedExceptionClass)
    }
  }

  it should "replace the exception with a value" in {
    val text = "A text"
    when {
      Single.error[String](new IllegalStateException())
        .onErrorReturn(new Func1[Throwable, String]() {
          override def call(t: Throwable): String = text
        })
    } assert {
      _.assertValue(text)
    }
  }

  "A Future" should "be converted to a single observable which contains the result returned by the future" in {
    val text = "Another text"

    //NOTE it is not possible to use a scala.concurrent.Future
    val future = executor.submit[Int](new Callable[Int] {
      override def call(): Int = text.length
    })

    when {
      Single.from(future)
    } assert {
      _.assertValue(text.length)
    }
  }

  "Two single observables" should "be concatenated to an observable that returns both values" in {
    val firstText = "123"
    val secondText = "456"

    when {
      val first = Single.just(firstText)
      val second = Single.just(secondText)

      first.concatWith(second)
    } assert {
      _.assertValues(firstText, secondText)
    }
  }

  it should "be merged to an observable that returns both values" in {
    val firstText = "123"
    val secondText = "456"

    when {
      val first = Single.just(firstText)
      val second = Single.just(secondText)

      first.mergeWith(second)
    } assert {
      _.assertValues(firstText, secondText)
    }
  }

  it should "be zipped to a single observable containing the result of a function call." in {
    val firstText = "123"
    val secondText = "456"
    val expectedResult = firstText + secondText

    when {
      val first = Single.just(firstText)
      val second = Single.just(secondText)

      first.zipWith(second, new Func2[String, String, String]() {
        override def call(t1: String, t2: String): String = t1 + t2
      })
    } assert {
      _.assertValues(expectedResult)
    }
  }

  "A single observable" should "not emit a value until a given scheduler triggers an action" in {
    val text = "A text"
    val scheduler = Schedulers.test()

    when {
      Single.just(text).subscribeOn(scheduler)
    } assert {
      _.assertNotCompleted()
    }
  }

  "A single observable" should "emit a value if a given scheduler has triggered an action" in {
    val text = "A text"
    val scheduler = Schedulers.test()

    when {
      Single.just(text).delay(1, TimeUnit.SECONDS, scheduler)
    } assert {
      subscriber => {
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        scheduler.triggerActions()

        subscriber.assertValue(text)
      }
    }
  }
}
