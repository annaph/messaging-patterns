package org.messaging.channel.invalid

import akka.actor.{Actor, ActorRef, Props}
import org.messaging.channel.invalid.InvalidMessageChannel._
import org.messaging.patterns.CompletableApp

object InvalidMessageChannelDriver extends CompletableApp(3) {
  val orderText = "<order id='13'>...</order>"
  val rawOrderBytes = orderText.toCharArray.map(_.toByte)

  val invalidMessageChannel = system.actorOf(
    Props[InvalidMessageChannel],
    "InvalidMessageChannel")

  val authenticator = system.actorOf(
    Props(classOf[Authenticator], invalidMessageChannel),
    "Authenticator")

  authenticator ! ProcessIncomingOrder(rawOrderBytes)
  authenticator ! new String("Not an Order")

  awaitCompletion()

  println(s"InvalidMessageChannelDriver: is completed.")
}

class Authenticator(invalidMessageChannel: ActorRef) extends Actor {
  override def receive: Receive = {
    case message: ProcessIncomingOrder =>
      val text = new String(message.orderInfo)
      println(s"Authenticator: processing $text")

      InvalidMessageChannelDriver.completedStep()

    case invalid: Any =>
      invalidMessageChannel ! InvalidMessage(sender(), self, invalid)

      InvalidMessageChannelDriver.completedStep()
  }
}

class InvalidMessageChannel extends Actor {
  override def receive: Receive = {
    case invalid: InvalidMessage =>
      println(s"InvalidMessageChannel: processing $invalid")

      InvalidMessageChannelDriver.completedStep()
  }
}

object InvalidMessageChannel {

  case class ProcessIncomingOrder(orderInfo: Array[Byte])

  case class InvalidMessage(sender: ActorRef,
                            receiver: ActorRef,
                            message: Any)

}
