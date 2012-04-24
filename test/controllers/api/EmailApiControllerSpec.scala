package controllers.api

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.immutable._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc.AnyContentAsJson
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EmailApiControllerSpec extends Specification {
  	"email api controller" should { 
			"throw an error in case of invalid parameters" in {
			  val map = Map("Content-Type" -> Seq("application/json"))
			  val content = new AnyContentAsJson(parse("{}"))
			  val result = EmailApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
			  status(result) must equalTo(OK)
			  contentType(result) must beSome("application/json")
			  val data = parse(contentAsString(result))
			  (data \ "status") must be equalTo(toJson("error"))
			}
			
			"return an error response in case of invalid authentication parameters" in {
					running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					  val map = Map("Content-Type" -> Seq("application/json"))
					  val paramMap = Map("username" -> "hdi", "password" -> "", "email" -> "hdhir@grassycreek.nl", "server" -> "https://email.grassycreek.nl/ews/Exchange.asmx" )
					  val content = new AnyContentAsJson(toJson(paramMap))
					  val result = EmailApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
					  status(result) must equalTo(OK)
					  contentType(result) must beSome("application/json")
					  val data = parse(contentAsString(result))
					  (data \ "status") must be equalTo(toJson("error"))
				}
			}
			
			"return an error if sending invalid parameters" in {
			  running(FakeApplication()) {
			  val map = Map("Content-Type" -> Seq("application/json"))
			  val paramMap = Map("from" -> "hdhir@grassycreek.nl", "to" -> "sagar.gopale@zuneeue.com", "cc" -> "pratik.gdk@gmail.com", "bcc" -> "pratik.gdk@gmail.com", "subject" -> "Test for Sending Email", "body" -> "Body of Email")
			  val content = new AnyContentAsJson(toJson(paramMap))
			  val result = EmailApiController.send(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
			  
			  status(result) must equalTo(OK)
			  contentType(result) must beSome("application/json")
			  val data = parse(contentAsString(result))
			  (data \ "status") must be equalTo(toJson("error"))  
			}
		}
			
			"return a success response in case of valid authentication parameters" in {
					running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					  val map = Map("Content-Type" -> Seq("application/json"))
					  val paramMap = Map("username" -> "hdir", "password" -> "socialite!", "email" -> "hdhir@grassycreek.nl", "server" -> "https://email.grassycreek.nl/ews/Exchange.asmx" )
					  val content = new AnyContentAsJson(toJson(paramMap))
					  val result = EmailApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
					  
					  // sleep for the thread so that the actor can do its job.
					  Thread.sleep(4000L)
					  status(result) must equalTo(OK)
					  contentType(result) must beSome("application/json")
					  val data = parse(contentAsString(result))
					  (data \ "status") must be equalTo(toJson("ok"))
					  
				}
			}
			
			"return an error response in case of invalid email id" in {
			  running(FakeApplication()) {
			    val result = EmailApiController.detail("eee2o2")(FakeRequest(GET, ""))
			    status(result) must equalTo(OK)
			    contentType(result) must beSome("application/json")
			    val data = parse(contentAsString(result))
			    (data \ "status") must be equalTo(toJson("error"))
			  }
			} 
			
			
  	}
}