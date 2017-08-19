package org.messaging.channel.deadletter

import akka.actor.{Actor, DeadLetter, PoisonPill, Props}
import org.messaging.patterns.CompletableApp

object DeadLetterChannelDriver extends CompletableApp(1) {
  val sysListener = system.actorOf(
    Props[SystemListener],
    "sysListener")

  val deadActor = system.actorOf(
    Props[DeadActor],
    "deadActor")

  system.eventStream.subscribe(sysListener, classOf[DeadLetter])

  deadActor ! PoisonPill
  deadActor ! new String("Not delivered message")

  awaitCompletion()

  println(s"DeadLetterChannelDriver: is completed.")
}

class SystemListener extends Actor {
  override def receive: Receive = {
    case deadLetter: DeadLetter =>
      println(s"SystemListener: processing $deadLetter")

      DeadLetterChannelDriver.completedStep()
  }
}

class DeadActor extends Actor {
  override def receive: Receive = {
    case message: Any =>
      println(s"DeadActor: processing $message")
  }
}
