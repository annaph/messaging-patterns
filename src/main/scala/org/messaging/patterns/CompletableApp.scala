package org.messaging.patterns

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, CoordinatedShutdown}

import scala.concurrent.ExecutionContext.Implicits.global

class CompletableApp(val steps: Int) extends App {

  val canComplete = new CountDownLatch(1)
  val canStart = new CountDownLatch(1)
  val completition = new CountDownLatch(steps)

  val system = ActorSystem("ReactiveEnterprise")

  def awaitCanCompleteNow(): Unit =
    canComplete.await()

  def awaitCanStartStartNow(): Unit =
    canStart.await()

  def awaitCompletion(): Unit = {
    completition.await()

    CoordinatedShutdown(system).run() foreach { _ =>
      println("Actor system 'ReactiveEnterprise' shutdown complete.")
    }
  }

  def completeNow(): Unit =
    canComplete.countDown()

  def startNow(): Unit =
    canStart.countDown()

  def completeAll(): Unit =
    while (completition.getCount > 0) {
      completition.countDown()
    }

  def completedStep(): Unit =
    completition.countDown()
}

object NoStepsDriver extends CompletableApp(0) {
  awaitCompletion()

  println("NoStepsDriver: is completed.")
}
