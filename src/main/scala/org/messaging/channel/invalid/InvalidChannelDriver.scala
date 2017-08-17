package org.messaging.channel.invalid

import akka.actor.{Actor, ActorRef, Props}
import org.messaging.patterns.CompletableApp

import InvalidMessageChannel._

object InvalidMessageChannelDriver extends CompletableApp(2) {
  val invalidMessageChannel = system.actorOf(
    Props[InvalidMessageChannel],
    "InvalidMessageChannel")

  val authenticator = system.actorOf(
    Props(classOf[Authenticator], invalidMessageChannel),
    "Authenticator")

  ???

  awaitCompletion()

  println(s"InvalidMessageChannelDriver: is completed.")
}

class Authenticator(invalidMessageChannel: ActorRef) extends Actor {
  override def receive: Receive = {
    ???
  }
}

class InvalidMessageChannel extends Actor {
  override def receive: Receive = {
    case invalid: InvalidMessage =>
      println(s"InvalidMessageCha")
      ???
  }
}

object InvalidMessageChannel {

  case class ProcessIncomingOrder(orderInfo: Array[Byte])

  case class InvalidMessage(sender: ActorRef,
                            receiver: ActorRef,
                            message: Any)
}
