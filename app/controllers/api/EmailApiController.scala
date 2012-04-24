package controllers.api

import play.api._
import play.api.mvc._
import play.api.libs.json.Json._
import play.libs.Akka
import akka.actor._
import akka.util._
import akka.dispatch._
import akka.dispatch.Await.CanAwait
import akka.util.duration._
import play.api.libs.concurrent._
import akka.pattern.ask
import play.api.Play.current
import microsoft.exchange.webservices.data._
import java.net.URI

import actors._
import models._

object EmailApiController extends Controller {
  
  
	implicit val timeout = Timeout(10 seconds) // needed for `?` below
	
	def setup = Action(parse.json) {
		implicit r => 
		  	val email = (r.body \ "email").asOpt[String].getOrElse("")
		  	val username = (r.body \ "username").asOpt[String].getOrElse("")
		  	val password = (r.body \ "password").asOpt[String].getOrElse("")
		  	val server = (r.body \ "server").asOpt[String].getOrElse("")
		  	
		  	if(email.isEmpty || username.isEmpty || password.isEmpty || server.isEmpty)
		  		Ok(toJson(Map("status" -> "error", "message" -> "Missing Parameters { email | username | password | server }")))
		  	else {
		  	  //instantiate user object
		  	  val u = new Account(email, username, password, server)
		  	 
		  	  //instantiate email auth actor
		  	  val emailAuthActor = Akka.system.actorOf(Props[ExchangeAuthenticationActor], name="emailAuthActor")
		  	  
		  	  //send the user object to the actor and wait on the result
		  	  val a = (emailAuthActor ? u).mapTo[Boolean].asPromise
		  	  a.await
		  	  
		  	  //got the result
		  	  if(a.value.get)
		  	  {
		  		 Account.save(u)
		  		 Ok(toJson(Map("status" -> "ok", "message" -> "User Setup Instantiated")))
		  	  }
		  	  else 
		  	     Ok(toJson(Map("status" -> "error", "message" -> "User Not Authorized")))
		  	}
	}
	
	
	
	def detail(id: String) = Action {
	  implicit r =>
	    val e = Email.findById(id)
	    if(e.isEmpty) {
	      Ok(toJson(Map("status" -> "error", "message" -> "email not found")))
	    }
	    else
	    	Ok(toJson(e.head))
	}
	
	def recent(userId: String, start: Int, limit: Int) = Action {
	  implicit r =>
	    val emails = Email.findByUser(userId, start, limit)
	    Ok(toJson(emails))
	}
	

	def send = Action(parse.json) {
	  implicit r =>
	    //val userId=(r.body \ "userId").asOpt[String].getOrElse("")
	    val from=(r.body \ "from").asOpt[String].getOrElse("")
	    val to=(r.body \ "to").asOpt[String].getOrElse("")
	    val cc=(r.body \ "cc").asOpt[String].getOrElse("")
	    val bcc=(r.body \ "bcc").asOpt[String].getOrElse("")
	    val subject=(r.body \ "subject").asOpt[String].getOrElse("")
	    val body=(r.body \ "body").asOpt[String].getOrElse("")
	    
	    val service = new ExchangeService()
	    val a = Account.findByEmail(from)
	    
	    
	    if(a.isEmpty) {
	      Ok(toJson(Map("status" -> "error", "message" -> "invalid user id")))
	    }
	    else {
	    	val account = a.head
		    val email = new EmailMessage(service)
		    val credentials = new WebCredentials(account.username, account.password, "")
	     	service.setCredentials(credentials)
	     	service.setUrl(new URI(account.serverURI))
	      
		    email.getToRecipients().add(to)
		    email.setSubject(subject)
		    email.setFrom(EmailAddress.getEmailAddressFromString(from))
		    email.setBody(MessageBody.getMessageBodyFromText(body))
		    Ok(toJson(Map("status" -> "success", "message" -> "email has been sent!")))
	    }

	    
	     
	    /*if(userId.isEmpty() || from.isEmpty() || to.isEmpty() || cc.isEmpty() || bcc.isEmpty() || subject.isEmpty())
	    {
	      Ok(toJson(Map("status" -> "error", "message" -> "Missing Parameters {from | to | cc | bcc | subject }")))
	    }
	    else
	    {
	      val s = Account.findById(userId)
	      val service = new ExchangeService()
	      
	      val msg = new EmailMessage(service)
	      msg.setFrom(new EmailAddress.getEmailAddressFromString(from))
	      msg.setBody(body)
	      msg.setSubject(subject)
	      /*EmailMessage msg= new EmailMessage(service);
	      msg.setSubject(subject); 
	      msg.setBody(MessageBody.getMessageBodyFromText(body));
	      msg.getToRecipients().add(to);
	      msg.sendAndSaveCopy();*/

	    }*/
	}
}