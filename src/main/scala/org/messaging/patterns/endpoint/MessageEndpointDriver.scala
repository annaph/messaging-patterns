package org.messaging.patterns.endpoint

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.messaging.patterns.CompletableApp
import org.messaging.patterns.endpoint.MessageEndpoint._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object MessageEndpointDriver extends CompletableApp(3) {
  implicit val timeout = Timeout(12 seconds)

  val discounter = system.actorOf(
    Props[ItemDiscountCalculator],
    "discounter")

  val endpoint = system.actorOf(
    Props(classOf[HighSierraPriceQuotes], discounter),
    "endpoint")

  endpoint ? RequestPriceQuote(1, 2, 3)

  awaitCompletion()

  println("MessageEndpointDriver: is completed.")
}

class HighSierraPriceQuotes(discounter: ActorRef) extends Actor {
  val quoterId: String = self.path.name

  override def receive: Receive = {
    case rpq: RequestPriceQuote =>
      println(s"HighSierraPriceQuotes: processing $rpq")

      discounter ! CalculateDiscountPriceFor(sender(),
        rpq.retailerId,
        rpq.rfqId,
        rpq.itemId)

      MessageEndpointDriver.completedStep()

    case pricing: DiscountPriceCalculated =>
      println(s"HighSierraPriceQuotes: processing $pricing")

      pricing.requestBy ! PriceQuote(quoterId,
        pricing.retailerId,
        pricing.rfqId,
        pricing.itemId,
        pricing.retailPrice,
        pricing.discountPrice)

      MessageEndpointDriver.completedStep()
  }
}

class ItemDiscountCalculator extends Actor {
  override def receive: Receive = {
    case message: CalculateDiscountPriceFor =>
      println(s"ItemDiscounterCalculator: processing $message")

      sender() ! DiscountPriceCalculated(message.sender,
        message.retailerId,
        message.rfqId,
        message.itemId,
        101.00,
        12.0)

      MessageEndpointDriver.completedStep()
  }
}

object MessageEndpoint {

  case class RequestPriceQuote(retailerId: Long,
                               rfqId: Long,
                               itemId: Long)

  case class PriceQuote(quoiterId: String,
                        retailerId: Long,
                        rfqId: Long,
                        itemId: Long,
                        retailPrice: Double,
                        discountPrice: Double)

  case class CalculateDiscountPriceFor(sender: ActorRef,
                                       retailerId: Long,
                                       rfqId: Long,
                                       itemId: Long)

  case class DiscountPriceCalculated(requestBy: ActorRef,
                                     retailerId: Long,
                                     rfqId: Long,
                                     itemId: Long,
                                     retailPrice: Double,
                                     discountPrice: Double)

}
