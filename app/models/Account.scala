package models

import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import javax.persistence.Id
import play.api.Play.current
import play.modules.mongodb.jackson.MongoDB
import scala.collection.JavaConversions._
import net.vz.mongodb.jackson.ObjectId

class Account(@ObjectId @Id val id: String,
			@BeanProperty @JsonProperty("email") val email: String,
		   @BeanProperty @JsonProperty("username") val username: String,
		   @BeanProperty @JsonProperty("password") val password: String,
		   @BeanProperty @JsonProperty("uri") val serverURI: String) {
    	@ObjectId @Id def getId = id
    	def this(email: String, username: String, password: String, uri: String) = this(org.bson.types.ObjectId.get.toString, email, username, password, uri)
}

object Account {
    private lazy val db = MongoDB.collection("accounts", classOf[Account], classOf[String])
    def save(user: Account) { db.save(user) }
    def findById(id: String) = Option(db.findOneById(id))
    def findByEmail(email: String) = db.find().is("email", email).limit(1).toArray.headOption
}