package controllers

import domain.{Consumer, ConsumerPersistence}
import play.api.libs.json.Json.toJson
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import reactivemongo.core.commands.LastError
import security.AsAdministrator
import utils.TokenGenerator.sha256
import utils.{ErrorMarshalling, ConsumerMarshalling}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Consumers extends Controller with MongoController with ConsumerPersistence
  with ConsumerMarshalling with ErrorMarshalling {

  def create = AsAdministrator(parse.json) { req =>
    req.body.validate[Request].asOpt.fold(Future(BadRequest(badRequestMsg))) { consumerReq =>
      val consumer = Consumer.fromName(consumerReq.consumer)
      persist(consumer.copy(token = sha256(consumer.token))).map {
        case le if le.ok => Created(toJson(Response(consumer._id, consumer.token, consumer.name)))
      }.recover {
        case LastError(_, _, Some(11000), _, _, _, _) => Conflict(conflictMsg(consumer.name))
        case _ => InternalServerError(internalServerErrorMsg)
      }
    }
  }

}
