package org.messaging.channel.guaranteeddelivery

import akka.actor.{Actor, ActorPath, ActorRef, Props}
import akka.pattern.ask
import akka.persistence.{AtLeastOnceDelivery, PersistentActor}
import akka.util.Timeout
import org.messaging.channel.guaranteeddelivery.GuaranteedDelivery._
import org.messaging.patterns.CompletableApp

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object GuaranteedDeliveryDriver extends CompletableApp(3) {
  implicit val timeout = Timeout(12 seconds)

  val loanRateQuote = system.actorOf(
    Props[LoanRateQuote],
    "loanRateQuote")

  val loanBroker = system.actorOf(
    Props(classOf[LoanBroker], loanRateQuote),
    "loanBroker")

  loanBroker ? QuoteBestLoanRate(1)

  awaitCompletion()

  println(s"GuaranteedDeliveryDriver: is completed.")
}

class LoanBroker(loanRateQuote: ActorRef) extends PersistentActor with AtLeastOnceDelivery {

  import scala.collection.mutable

  override val persistenceId: String = "LoanBroker"

  private var processes: mutable.Map[Int, LoanRateQuoteRequested] = mutable.Map()

  override def receiveCommand: Receive = {
    case command: QuoteBestLoanRate =>
      println(s"LoanBroker: processing $command")
      if (!duplicate(command)) {
        val loanRateQuoteId = LoanRateQuote id command.id
        startProcess(command.id, loanRateQuoteId, loanRateQuote)
      }

      GuaranteedDeliveryDriver.completedStep()

    case started: LoanRateQuoteStarted =>
      println(s"LoanBroker: processing $started")
      persist(started) { ack =>
        confirmDelivery(ack.id)
      }

      GuaranteedDeliveryDriver.completedStep()
  }

  override def receiveRecover: Receive = {
    case event: LoanRateQuoteRequested =>
      updateWith(event)
      deliver(event.loanRateQuotePath) { id =>
        StartLoanRateQuote(id)
      }

    case started: LoanRateQuoteStarted =>
      confirmDelivery(started.id)
  }

  private def duplicate(command: QuoteBestLoanRate): Boolean = {
    val duplicateFound = processes contains command.id
    if (duplicateFound) {
      val loanRateQuoteRequested = processes(command.id)
      sender() ! loanRateQuoteRequested
    }

    duplicateFound
  }

  private def startProcess(quoteRequestId: Int, loanRateQuoteId: String, loanRateQuote: ActorRef): Unit = {
    val loanRateQuotePath = loanRateQuote.path
    val loanRateQuoteRequested = LoanRateQuoteRequested(quoteRequestId, loanRateQuoteId, loanRateQuotePath)

    persist(loanRateQuoteRequested) { event =>
      updateWith(event)
      deliver(loanRateQuotePath) { id =>
        StartLoanRateQuote(id)
      }

      sender() ! event
    }
  }

  def updateWith(event: LoanRateQuoteRequested): Unit =
    if (!processes.contains(event.quoteRequestId)) {
      processes += (event.quoteRequestId -> event)
    }
}

class LoanRateQuote extends Actor {

  override def receive: Receive = {
    case rq: StartLoanRateQuote =>
      println(s"LoanRateQuote: pocessing $rq")
      sender() ! LoanRateQuoteStarted(rq.id)

      GuaranteedDeliveryDriver.completedStep()
  }
}

object LoanRateQuote {

  def id(commandId: Int): String =
    "loanRateQuoteId-" + commandId
}

object GuaranteedDelivery {

  case class QuoteBestLoanRate(id: Int)

  case class LoanRateQuoteRequested(quoteRequestId: Int,
                                    loanRateQuoteId: String,
                                    loanRateQuotePath: ActorPath)

  case class StartLoanRateQuote(id: Long)

  case class LoanRateQuoteStarted(id: Long)

}
