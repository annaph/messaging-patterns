package org.messaging.patterns.pipes

import akka.actor.{Actor, ActorRef, Props}
import org.messaging.patterns.CompletableApp

case class ProcessIncomingOrder(orderInfo: Array[Byte])

object PipesAndFiltersDriver extends CompletableApp(9) {
  val orderText = "(encryption)(certificate)<order id='123'>...</order>"
  val rawOrderBytes = orderText.toCharArray.map(_.toByte)

  val filter5 = system.actorOf(
    Props[OrderManagementSystem],
    "orderManagementSystem")

  val filter4 = system.actorOf(
    Props(classOf[Deduplicator], filter5),
    "deduplicator")

  val filter3 = system.actorOf(
    Props(classOf[Authenticator], filter4),
    "authenticator")

  val filter2 = system.actorOf(
    Props(classOf[Decrypter], filter3),
    "decrypter")

  val filter1 = system.actorOf(
    Props(classOf[OrderAcceptanceEndpoint], filter2),
    "orderAcceptanceEndpoint")

  filter1 ! rawOrderBytes
  filter1 ! rawOrderBytes

  awaitCompletion()

  println("PipesAndFiltersDriver: is completed.")
}

class OrderAcceptanceEndpoint(nextFilter: ActorRef) extends Actor {
  override def receive: Receive = {
    case rawOrder: Array[Byte] =>
      val text = new String(rawOrder)
      println(s"OrderAcceptanceEndpoint: processing $text")

      nextFilter ! ProcessIncomingOrder(rawOrder)

      PipesAndFiltersDriver.completedStep()
  }
}

class Decrypter(nextFilter: ActorRef) extends Actor {
  override def receive: Receive = {
    case message: ProcessIncomingOrder =>
      val text = new String(message.orderInfo)
      println(s"Decrypter: processing $text")

      val orderText = text replace("(encryption)", "")
      nextFilter ! ProcessIncomingOrder(orderText.toCharArray.map(_.toByte))

      PipesAndFiltersDriver.completedStep()
  }
}

class Authenticator(nextFilter: ActorRef) extends Actor {
  override def receive: Receive = {
    case message: ProcessIncomingOrder =>
      val text = new String(message.orderInfo)
      println(s"Authenticator: processing $text")

      val orderText = text replace("(certificate)", "")
      nextFilter ! ProcessIncomingOrder(orderText.toCharArray.map(_.toByte))

      PipesAndFiltersDriver.completedStep()
  }
}

class Deduplicator(nextFilter: ActorRef) extends Actor {

  import scala.collection.mutable

  val processedOrderIds = mutable.Set[String]()

  override def receive: Receive = {
    case message: ProcessIncomingOrder =>
      val text = new String(message.orderInfo)
      println(s"Deduplicator: processing $text")

      val orderId = orderIdFrom(text)
      if (processedOrderIds add orderId)
        nextFilter ! message
      else
        println(s"Deduplicator: found duplicate order $orderId")

      PipesAndFiltersDriver.completedStep()
  }

  def orderIdFrom(orderText: String): String = {
    val orderIdIndex = (orderText indexOf "id='") + 4
    val orderIdLAstIndex = orderText indexOf("'", orderIdIndex)

    orderText substring(orderIdIndex, orderIdLAstIndex)
  }
}

class OrderManagementSystem extends Actor {
  override def receive: Receive = {
    case message: ProcessIncomingOrder =>
      val text = new String(message.orderInfo)
      println(s"OrderManagementSystem: processing unique order: $text")

      PipesAndFiltersDriver.completedStep()
  }
}
