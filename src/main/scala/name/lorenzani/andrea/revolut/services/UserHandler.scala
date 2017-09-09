package name.lorenzani.andrea.revolut.services

import com.twitter.finagle.http._
import com.twitter.finagle.http.path._
import name.lorenzani.andrea.revolut.datastore.{DataStore, Size, User}
import name.lorenzani.andrea.revolut.json.JsonUtil

import scala.util.{Failure, Success, Try}

class UserHandler(ds: DataStore) extends RequestHandler {

  override def route(request: Request): PartialFunction[Path, Try[String]] = {
    case Root => showAllUsers()
    case Root / "user" / id if request.method == Method.Get => showUserId(id)
    case Root / "user" / name / id if request.method == Method.Post =>
      addUser(name, Some(id))
    case Root / "user" / name if request.method == Method.Post =>
      addUser(name)
    case Root / "size" => getSize
  }

  def addUser(name: String, id: Option[String] = None) = Try {
    JsonUtil.toJson(ds.add(User(id.getOrElse(ds.size.toString), name, 0)))
  }

  def showUserId(id: String): Try[String] = ds.getUser(id) match {
    case Some(res) => Success(JsonUtil.toJson(res))
    case None => Failure(new IllegalArgumentException("Not found"))
  }

  def showAllUsers(): Try[String] = Try {
    JsonUtil.toJson(ds.getAll)
  }

  def getSize: Try[String] = Try {
    val size = ds.size
    JsonUtil.toJson(Size(size))
  }
}
