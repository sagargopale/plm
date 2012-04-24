package models

import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import javax.persistence.Id
import play.api.Play.current
import play.modules.mongodb.jackson.MongoDB
import scala.collection.JavaConversions._
import net.vz.mongodb.jackson.ObjectId
import play.api.libs.json._
import play.api.libs.json.Json._

class Email(@ObjectId @Id val id: String,
			@BeanProperty @JsonProperty("userId") val userId: String,
			@BeanProperty @JsonProperty("from") val from: String,
			@BeanProperty @JsonProperty("to") val to: List[String],
			@BeanProperty @JsonProperty("cc") val cc: List[String],
			@BeanProperty @JsonProperty("bcc") val bcc: List[String],
			@BeanProperty @JsonProperty("subject") val subject: String,
			@BeanProperty @JsonProperty("body") val body: String,
			@BeanProperty @JsonProperty("exchangeId") val exchangeId: String,
			@BeanProperty @JsonProperty("sentdate") val sentDate: Long) {
    	@ObjectId @Id def getId = id
    	def this(uid: String, from: String, to: List[String], cc: List[String], bcc: List[String], subject: String, body: String, exchangeId: String, sentDate: Long) = this(org.bson.types.ObjectId.get.toString, uid, from, to, cc, bcc, subject, body, exchangeId, sentDate)
}

object Email {
    private lazy val db = MongoDB.collection("emails", classOf[Email], classOf[String])
    def save(e: Email) { db.save(e) }
    def findById(id: String) = {
      try {
    	  Option(db.findOneById(id))
      }
      catch {
        case e: IllegalArgumentException => Option.empty[Email]
      }
    }
    def findByUser(userId: String) = db.find().is("uid", userId).toArray.toList
    
    def findByUser(userId: String, start: Int, limit: Int) = db.find().is("userId", userId).skip(start).limit(limit).toArray.toList
     
    implicit object EmailReads extends Format[Email] {
	    def reads(json: JsValue) = new Email(
	      (json \ "id").as[String],
	      (json \ "uid").as[String],
	      (json \ "from").as[String],
	      (json \ "to").as[List[String]],
	      (json \ "cc").as[List[String]],
	      (json \ "bcc").as[List[String]],
	      (json \ "subject").as[String],
	      (json \ "body").as[String],
	      (json \ "exchangeId").as[String],
	      (json \ "sentdate").as[Long])
	    
	    def writes(e: Email) = JsObject(Seq(
	      "id" -> JsString(e.id),
	      "uid" -> JsString(e.userId),
	      "from" -> JsString(e.from),
	      "to" -> JsArray(e.to.map(toJson(_))),
	      "cc" -> JsArray(e.cc.map(toJson(_))),
	      "bcc" -> JsArray(e.bcc.map(toJson(_))),
	      "subject" -> JsString(e.subject),
	      "body" -> JsString(e.body),
	      "exchangeId" -> JsString(e.exchangeId),
	      "sentdate" -> JsNumber(e.sentDate)))
    }
}