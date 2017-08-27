package org.messaging.channel.messagebridge

object messages {

  case class TranslateInventoryProductAllocation(jsonText: String)

  case class InventoryProductAllocationTranslated()
}
