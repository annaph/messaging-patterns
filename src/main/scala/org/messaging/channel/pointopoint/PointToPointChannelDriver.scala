package org.messaging.channel.pointopoint

import akka.actor.{Actor, ActorRef, Props}
import org.messaging.channel.pointopoint.PointToPointChannel.{StartA, StartB}
import org.messaging.patterns.CompletableApp

object PointToPointChannelDriver extends CompletableApp(6) {
  val c = system.actorOf(
    Props[ActorC],
    "actorC")

  val a = system.actorOf(
    Props(classOf[ActorA], c),
    "actorA")

  val b = system.actorOf(
    Props(classOf[ActorB], c),
    "actorB")

  a ! StartA()
  b ! StartB()

  awaitCompletion()

  println("PointToPointChannelDriver: is completed.")
}

class ActorA(actorC: ActorRef) extends Actor {

  override def receive: Receive = {
    case message: StartA =>
      println("ActorA: sending Hello messages to Actor C...")

      actorC ! "Hello, from actor A!"
      actorC ! "Hello again, from actor A!"

      PointToPointChannelDriver.completedStep()
  }
}

class ActorB(actorC: ActorRef) extends Actor {

  override def receive: Receive = {
    case message: StartB =>
      println("ActorB: sending goodbye messages to Actor C...")

      actorC ! "Goodbye, from actor B!"
      actorC ! "Goodbye again, from actor B!"

      PointToPointChannelDriver.completedStep()
  }
}

class ActorC extends Actor {
  var hello = 0
  var helloAgain = 0
  var goodbye = 0
  var goodbyeAgain = 0

  override def receive: Receive = {
    case message: String =>
      println(s"ActorC: processing $message")

      hello = hello + {
        if (message contains "Hello") 1 else 0
      }

      helloAgain = helloAgain + {
        if (message startsWith "Hello again") 1 else 0
      }

      goodbye = goodbye + {
        if (message contains "Goodby") 1 else 0
      }

      goodbyeAgain = goodbyeAgain + {
        if (message startsWith "Goodbye again") 1 else 0
      }

      PointToPointChannelDriver.completedStep()
  }
}

object PointToPointChannel {

  case class StartA()

  case class StartB()

}