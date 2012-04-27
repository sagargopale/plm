package actors

import akka.actor.Actor

import play.api._
import collection.JavaConversions._
import microsoft.exchange.webservices.data._
import java.net.URI
import play.api.Play.current

import models._

class EmailSendingActor extends Actor{
  val service = new ExchangeService();
  
  def receive = {
    case u: Account =>
      val credentials = new WebCredentials(u.username, u.password, "")
      service.setCredentials(credentials)
      service.setUrl(new URI(u.serverURI)
      
      
  }
    
}
