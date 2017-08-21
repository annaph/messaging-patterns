package org.messaging.channel.guaranteeddelivery

import akka.persistence.{AtLeastOnceDelivery, PersistentActor}
import org.messaging.patterns.CompletableApp
import akka.actor.{Actor, ActorPath, ActorRef}
import GuaranteedDelivery._

object GuaranteedDeliveryDriver extends CompletableApp(???) {

}

class LoanBroker(loanRateQuote: ActorRef) extends PersistentActor with AtLeastOnceDelivery {
  import scala.collection.mutable

  override val persistenceId: String = "LoanBroker"

  private var processes: mutable.Map[Int, LoanRateQuoteRequested] = mutable.Map()

  override def receiveCommand: Receive = {
    case command: QuoteBestLoanRate =>
      if (!duplicate(command)) {
        val loanRateQuoteId = LoanRateQuote id command.id
        startProcess(command.id, loanRateQuoteId, loanRateQuote)
      }

    case started: LoanRateQuoteStarted =>
      persist(started) { ack =>
        confirmDelivery(ack.id)
      }
  }

  override def receiveRecover: Receive = {
    ???
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
      ???
      sender() ! LoanRateQuoteStarted(rq.id)
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

  case class LoanRateQuoteStarted(id : Long)

}
