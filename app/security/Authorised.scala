package security

import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc.Results._
import play.api.mvc._
import utils.ErrorMarshalling

object Authorised extends ErrorMarshalling {

  def apply()(f: Request[AnyContent] => Result) = Action(secured(f))
  
  def apply(parser: BodyParser[JsValue])(f: Request[JsValue] => Result) = Action(parser)(secured(f))
  
  private def secured[T](f: Request[T] => Result) = { req: Request[T] =>
    req.headers.get("admin_token").fold(Forbidden(forbiddenMsg)) {
      case s if s == secret => f(req)
      case _ => Forbidden(forbiddenMsg)
    }
  }
  
  private def secret = Option(System.getenv("ADMIN_TOKEN")).getOrElse("default_token")

}