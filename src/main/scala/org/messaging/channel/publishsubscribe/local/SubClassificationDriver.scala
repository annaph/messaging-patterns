package org.messaging.channel.publishsubscribe.local

import java.math.BigDecimal

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification
import org.messaging.channel.publishsubscribe.local.SubClassification._
import org.messaging.patterns.CompletableApp

object SubClassificationDriver extends CompletableApp(6) {
  val allSubscriber = system.actorOf(
    Props[AllMarketsSubscriber],
    "AllMarketsSubscriber")

  val nasdaqSubscriber = system.actorOf(
    Props[NASDAQSubscriber],
    "NASDAQSubscriber")

  val nyseSubscriber = system.actorOf(
    Props[NYSESubscriber],
    "NYSESubscriber")

  val quotesBus = new QuotesEventBus

  quotesBus subscribe(allSubscriber, Market("quotes"))
  quotesBus subscribe(nasdaqSubscriber, Market("quotes/NASDAQ"))
  quotesBus subscribe(nyseSubscriber, Market("quotes/NYSE"))

  quotesBus publish PriceQuoted(
    Market("quotes/NYSE"),
    'ORLC,
    new Money("37.84"))

  quotesBus publish PriceQuoted(
    Market("quotes/NASDAQ"),
    'MSFT,
    new Money("37.16"))

  quotesBus publish PriceQuoted(
    Market("quotes/DAX"),
    Symbol("SAP:GR"),
    new Money("61.95"))

  quotesBus publish PriceQuoted(
    Market("quotes/NKY"),
    Symbol("6701:JP"),
    new Money("237"))

  awaitCompletion()

  println("SubClassificationDriver: is completed.")
}

class QuotesEventBus extends EventBus with SubchannelClassification {
  type Classifier = Market
  type Event = PriceQuoted
  type Subscriber = ActorRef

  override def classify(event: Event): Classifier =
    event.market

  override def subclassification: Subclassification[Classifier] =
    new Subclassification[Market] {
      override def isEqual(subscribedToClassifier: Market, eventClassifier: Market): Boolean =
        eventClassifier equals subscribedToClassifier

      override def isSubclass(subscribedToClassifier: Market, eventClassifier: Market): Boolean =
        subscribedToClassifier.name startsWith eventClassifier.name
    }

  override def publish(event: Event, subscriber: Subscriber): Unit =
    subscriber ! event
}

class AllMarketsSubscriber extends Actor {
  override def receive: Receive = {
    case quote: PriceQuoted =>
      println(s"AllMarketsSubscriber received: $quote")
      SubClassificationDriver.completedStep()
  }
}

class NASDAQSubscriber extends Actor {
  override def receive: Receive = {
    case quote: PriceQuoted =>
      println(s"NASDAQSubsciber received: $quote")
      SubClassificationDriver.completedStep()
  }
}

class NYSESubscriber extends Actor {
  override def receive: Receive = {
    case quote: PriceQuoted =>
      println(s"NYSESubscriber received: $quote")
      SubClassificationDriver.completedStep()
  }
}

object SubClassification {

  case class Money(amount: BigDecimal) {
    def this(amount: String) =
      this(new BigDecimal(amount))

    amount setScale(4, BigDecimal.ROUND_HALF_UP)
  }

  case class Market(name: String)

  case class PriceQuoted(market: Market,
                         ticker: Symbol,
                         price: Money)

}
