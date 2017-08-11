package org.messaging.patterns.translator

import akka.actor.{Actor, Props}
import org.messaging.patterns.CompletableApp

object MessageTranslatorDriver extends CompletableApp(???) {
  val translator = system.actorOf(
    Props[Translator],
    "translator")
}

class Translator extends Actor {
  override def receive: Receive = {
    case message: Array[Byte] =>
      val text = new String(message.map(_.toByte))
      println(s"Translator: translated to $text")

      ???
  }
}