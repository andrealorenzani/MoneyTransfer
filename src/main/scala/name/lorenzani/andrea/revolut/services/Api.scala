package name.lorenzani.andrea.revolut.services

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import name.lorenzani.andrea.revolut.datastore.{DataStore, Size, StoreError, User}
import name.lorenzani.andrea.revolut.json.Json
import name.lorenzani.andrea.revolut.json.Json.given

import scala.util.Try

final case class ApiResponse(status: Int, body: String, contentType: String)

/** The REST routes, independent of the HTTP server so they can be tested directly. */
class Api(store: DataStore):

  def handle(method: String, path: String): ApiResponse =
    val segments = path.split('/').filter(_.nonEmpty).toList
    (method, segments) match
      case ("GET", Nil)                           => json(store.all)
      case (_, "size" :: Nil)                     => json(Size(store.size))
      case ("GET", "user" :: id :: Nil)           => store.get(id).fold(notFound)(json(_))
      case ("POST", "user" :: name :: id :: Nil)  => created(store.add(User(id, name, 0)))
      case ("POST", "user" :: name :: Nil)        => created(store.add(User(store.size.toString, name, 0)))
      case ("PUT", "add" :: id :: amount :: Nil)  => withAmount(amount)(a => result(store.addMoney(id, a)))
      case ("PUT", "move" :: source :: amount :: dest :: Nil) =>
        withAmount(amount)(a => result(store.transfer(source, dest, a)))
      case _ => notFound

  private def withAmount(raw: String)(f: BigDecimal => ApiResponse): ApiResponse =
    Try(BigDecimal(raw)).toOption.fold(badRequest(s"'$raw' is not a valid amount"))(f)

  private def result[A: JsonValueCodec](outcome: Either[StoreError, A]): ApiResponse =
    outcome.fold(error => badRequest(error.message), json(_))

  private def created(outcome: Either[StoreError, User]): ApiResponse =
    outcome.fold(error => badRequest(error.message), user => json(user).copy(status = 201))

  private def json[A: JsonValueCodec](value: A): ApiResponse =
    ApiResponse(200, Json.toJson(value), "application/json")

  private def badRequest(message: String) = ApiResponse(400, message, "text/plain; charset=utf-8")

  private val notFound = ApiResponse(404, "Not found", "text/plain; charset=utf-8")
