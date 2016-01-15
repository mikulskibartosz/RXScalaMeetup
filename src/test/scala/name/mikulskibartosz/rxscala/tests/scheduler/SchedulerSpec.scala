package name.mikulskibartosz.rxscala.tests.scheduler

import java.util
import java.util.concurrent._

import com.google.common.util.concurrent.MoreExecutors
import org.scalatest.{FlatSpec, Matchers}
import rx.lang.scala.{Scheduler, Worker}
import rx.lang.scala.schedulers.{ImmediateScheduler, TestScheduler, TrampolineScheduler}
import rx.schedulers.Schedulers

import scala.collection.JavaConversions

class SchedulerSpec extends FlatSpec with Matchers {
  "The scheduler factory" should "always return the same instance of predefined types" in {
    //NOTE the scheduler factory method create an instance of a "java" scheduler,
    //"scala" schedulers extend the java classes
    Schedulers.immediate() should be theSameInstanceAs Schedulers.immediate()
    Schedulers.computation() should be theSameInstanceAs Schedulers.computation()
    Schedulers.io() should be theSameInstanceAs Schedulers.io()
    Schedulers.newThread() should be theSameInstanceAs Schedulers.newThread()
    Schedulers.trampoline() should be theSameInstanceAs Schedulers.trampoline()
  }

  "The scheduler factory" should "return a new instance of test scheduler and executor based scheduler" in {
    val executor = MoreExecutors.newDirectExecutorService()

    Schedulers.from(executor) should not be theSameInstanceAs(Schedulers.from(executor))
    Schedulers.test() should not be theSameInstanceAs(Schedulers.test())
  }

  //Schedulers that do not create a thread, hence the order of elements is predictable:

  "The immediate scheduler" should "execute the work immediately on the current thread" in {
    testSchedulerUsingSingleAction(ImmediateScheduler()){
      _.toStream should contain theSameElementsInOrderAs
        Vector(1, 2, 3)
    }
  }

  "The test scheduler" should "not execute work if the caller has not called the triggerActions method" in {
    val scheduler = TestScheduler()
    testSchedulerUsingSingleAction(scheduler){
      iterator => iterator.toStream should contain theSameElementsInOrderAs Vector(1, 3)
    }
  }

  "The test scheduler" should "execute work when the caller has called the triggerActions method" in {
    val scheduler = TestScheduler()
    testSchedulerUsingSingleAction(scheduler){
      iterator => {
        scheduler.triggerActions()
        iterator.toStream should contain theSameElementsInOrderAs Vector(1, 3, 2)
      }
    }
  }

  //The trampoline scheduler uses the same thread to execute all actions, therefore the order of elements is constant
  "The trampoline scheduler" should "execute actions in a constant order" in {
    val addElements: (Worker, util.Queue[Int]) => Unit = (worker, queue) => {
      for(i <- 1 to 3) {
        worker.schedule({queue.add(i); ()})
      }
    }

    testScheduler(TrampolineScheduler())(addElements)(_.toStream should contain theSameElementsInOrderAs Vector(1, 2, 3))
  }

  private def testSchedulerUsingSingleAction(scheduler: Scheduler)(assert: Iterator[Int] => Unit): Unit = {
    val addElements: (Worker, util.Queue[Int]) => Unit = (worker, queue) => {
      queue.add(1)

      worker.schedule({queue.add(2); ()})
      queue.add(3)
    }

    testScheduler(scheduler)(addElements)(assert)
  }

  private def testScheduler(scheduler: Scheduler)(addElements: (Worker, util.Queue[Int]) => Unit)(assert: Iterator[Int] => Unit): Unit = {
    withSingleThreadExecutor {
      executor => {
        val queue = new ConcurrentLinkedQueue[Int]()

        val future = executor.submit(new Runnable {
          override def run(): Unit = addElements(scheduler.createWorker, queue)
        })
        future.get()

        //due to weak consistency of the ConcurrentLinkedQueue iterator it returns elements added to the queue after calling the iterator method
        val iterator = JavaConversions.asScalaIterator(queue.iterator())
        assert(iterator)
      }
    }
  }

  private def withSingleThreadExecutor(body: ScheduledExecutorService => Unit): Unit = {
    val executor = Executors.newSingleThreadScheduledExecutor()
    try {
      body(executor)
    } finally {
      executor.shutdown()
    }
  }
}
