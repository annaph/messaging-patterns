package org.messaging.channel.guaranteeddelivery

import akka.persistence.{AtLeastOnceDelivery, PersistentActor}
import org.messaging.patterns.CompletableApp

object GuaranteedDeliveryDriver extends CompletableApp(???) {

}

class LoanBroker extends PersistentActor with AtLeastOnceDelivery {
  override val persistenceId: String = "LoanBroker"

  override def receiveCommand: Receive = {
    ???
  }

  override def receiveRecover: Receive = {
    ???
  }
}
