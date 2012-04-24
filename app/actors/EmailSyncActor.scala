package actors

import akka.actor.Actor

import play.api._
import collection.JavaConversions._
import microsoft.exchange.webservices.data._
import java.net.URI
import play.api.Play.current


import models._

class EmailSyncActor extends Actor {
  
	  val service = new ExchangeService();
	  
	  def receive = {
	    /** to start the synchronization process **/
	    case u: Account =>
	      
	      val credentials = new WebCredentials(u.username, u.password, "")
	      service.setCredentials(credentials)
	      service.setUrl(new URI(u.serverURI))
	      val view = new ItemView(50)
	      val results = service.findItems(WellKnownFolderName.Inbox, view)
	      val subjectList = results.getItems().foreach {
	        i =>
	          val message = EmailMessage.bind(service, i.getId);
	          message.load
	          val e = new Email(u.id, message.getFrom().getAddress(), message.getToRecipients().map{_.getAddress()}.toList, message.getCcRecipients().map{_.getAddress()}.toList, message.getBccRecipients().map{_.getAddress()}.toList, message.getSubject(), message.getBody().toString(), message.getId().toString(), message.getDateTimeReceived().getTime())
	          Email.save(e)
	      }
	  }
}