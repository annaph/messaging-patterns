package org.messaging.channel.datatype

import akka.actor.{Actor, Props}
import org.messaging.channel.datatype.DatatypeChannel._
import org.messaging.patterns.CompletableApp

object DatatypeChannelDriver extends CompletableApp(3) {
  val productQuery = "ProductQuery message"
  val rawProductQueryBytes = productQuery.toCharArray.map(_.toByte)

  val priceQuote = "PriceQuote message"
  val rawPriceQuoteBytes = priceQuote.toCharArray.map(_.toByte)

  val purchaseOrder = "PurchaseOrder message"
  val rawPurchaseOrderBytes = purchaseOrder.toCharArray.map(_.toByte)

  val productQueriesChannel = system.actorOf(
    Props[ProductQueriesChannel],
    "ProductQueriesChannel")

  val priceQuoteChannel = system.actorOf(
    Props[PriceQuoteChannel],
    "PriceQuoteChannel")

  val purchaseOrderChannel = system.actorOf(
    Props[PurchaseOrderChannel],
    "PurchaseOrderChannel")

  subscribers("ProductQueriesChannel") onMessage JMSMessage("1", rawProductQueryBytes)
  subscribers("PriceQuoteChannel") onMessage JMSMessage("2", rawPriceQuoteBytes)
  subscribers("PurchaseOrderChannel") onMessage JMSMessage("3", rawPurchaseOrderBytes)

  awaitCompletion()

  println("DatatypeChannelDriver: is completed.")
}

abstract class RabitMQReceiver extends Actor {
  subscribers put(this.getClass.getSimpleName, this)

  def onMessage(jmsMessage: JMSMessage): Unit =
    self ! jmsMessage.body
}

class ProductQueriesChannel extends RabitMQReceiver {
  override def receive: Receive = {
    case message: Array[Byte] =>
      val productQuery = translateToProductQuery(message)
      println(s"ProductQueriesChannel: processing ${productQuery.text}")

      DatatypeChannelDriver.completedStep()
  }

  private def translateToProductQuery(message: Array[Byte]): ProductQuery =
    ProductQuery(new String(message))
}

class PriceQuoteChannel extends RabitMQReceiver {
  override def receive: Receive = {
    case message: Array[Byte] =>
      val priceQuote = translateToPriceQuote(message)
      println(s"PriceQuoteChannel: processing ${priceQuote.text}")

      DatatypeChannelDriver.completedStep()
  }

  private def translateToPriceQuote(message: Array[Byte]): PriceQuote =
    PriceQuote(new String(message))
}

class PurchaseOrderChannel extends RabitMQReceiver {
  override def receive: Receive = {
    case message: Array[Byte] =>
      val purchaseProduct = translateToPurchaseOrder(message)
      println(s"PurchaseOrderChannel: processing ${purchaseProduct.text}")

      DatatypeChannelDriver.completedStep()
  }

  private def translateToPurchaseOrder(message: Array[Byte]): PurchaseOrder =
    PurchaseOrder(new String(message))
}

object DatatypeChannel {

  import scala.collection.mutable

  var subscribers: mutable.Map[String, RabitMQReceiver] = mutable.Map()

  case class JMSMessage(id: String,
                        body: Array[Byte])

  case class ProductQuery(text: String)

  case class PriceQuote(text: String)

  case class PurchaseOrder(text: String)

}
