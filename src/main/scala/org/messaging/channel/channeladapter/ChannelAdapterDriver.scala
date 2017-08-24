package org.messaging.channel.channeladapter

import akka.actor.{Actor, ActorRef, Props}
import org.messaging.channel.channeladapter.messages._
import org.messaging.patterns.CompletableApp

object messages {

  case class ExecuteBuyOrder(portfolioId: Long,
                             symbol: Symbol,
                             quantity: Int,
                             price: Double)

  case class ExecuteSellOrder(portfolioId: Long,
                              symbol: Symbol,
                              quantity: Int,
                              price: Double)

  trait OrderExecuted

  case class BuyOrderExecuted(portfolioId: Long,
                              orderId: String,
                              quantity: Int,
                              totalCost: Double)
    extends OrderExecuted

  case class SellOrderExecuted(portfolioId: Long,
                               orderId: String,
                               quantity: Int,
                               totalCost: Double)
    extends OrderExecuted

  case class BuyerServiceResult(portfolioId: Long,
                                orderId: String,
                                quantity: Int,
                                totalCost: Double)

  case class SellerServiceResult(portfolioId: Long,
                                 orderId: String,
                                 quantity: Int,
                                 totalCost: Double)

  case class RegisterCommandHandler(aplicationId: String,
                                    aplicationName: String,
                                    ref: ActorRef)

  case class TradingNotification(name: String, event: OrderExecuted)

}

object ChannelAdapterDriver extends CompletableApp(6) {
  val tradingBus = system.actorOf(
    Props[TradingBus],
    "tradingBus")

  val stockTrader = system.actorOf(
    Props(classOf[StockTrader], tradingBus),
    "stockTrader")

  stockTrader ! ExecuteBuyOrder(123, 'skirt, 1, 12)
  stockTrader ! ExecuteSellOrder(756, 'heels, 2, 47)

  awaitCompletion()

  println("ChannelAdapterDriver: is completed.")
}

class StockTrader(tradingBus: ActorRef) extends Actor {
  val applicationId = self.path.name

  tradingBus ! RegisterCommandHandler(applicationId, "ExecuteBuyOrder", self)
  tradingBus ! RegisterCommandHandler(applicationId, "ExecuteSellOrder", self)

  override def receive: Receive = {
    case buy: ExecuteBuyOrder =>
      println(s"StockTrader: processing $buy")
      val result = BuyerService.placeBuyOrder(buy.portfolioId, buy.symbol, buy.quantity, buy.price)
      tradingBus ! TradingNotification(
        "BuyOrderExecuted",
        BuyOrderExecuted(result.portfolioId, result.orderId, result.quantity, result.totalCost))

      ChannelAdapterDriver.completedStep()

    case sell: ExecuteSellOrder =>
      println(s"StockTrader: processing $sell")
      val result = SellerService.placeSellOrder(sell.portfolioId, sell.symbol, sell.quantity, sell.price)
      tradingBus ! TradingNotification(
        "SellOrderExecuted",
        SellOrderExecuted(result.portfolioId, result.orderId, result.quantity, result.totalCost))

      ChannelAdapterDriver.completedStep()
  }
}

class TradingBus extends Actor {
  override def receive: Receive = {
    case rq: RegisterCommandHandler =>
      println(s"TradingBus: processing $rq")

      ChannelAdapterDriver.completedStep()

    case notification: TradingNotification =>
      println(s"TradingBus: processing $notification")

      ChannelAdapterDriver.completedStep()
  }
}

object BuyerService {
  def placeBuyOrder(portfolioId: Long, symbol: Symbol, quantity: Int, price: Double): BuyerServiceResult =
    BuyerServiceResult(portfolioId, "order-" + symbol.toString.drop(1), quantity, price)
}

object SellerService {
  def placeSellOrder(portfolioId: Long, symbol: Symbol, quantity: Int, price: Double): SellerServiceResult =
    SellerServiceResult(portfolioId, "order-" + symbol.toString.drop(1), quantity, price)
}
