package name.lorenzani.andrea.revolut.services

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path._
import name.lorenzani.andrea.revolut.datastore.DataStore
import name.lorenzani.andrea.revolut.json.JsonUtil

import scala.util.{Failure, Success, Try}

class TransferHandler(ds: DataStore) extends RequestHandler {
  override def route(request: Request): PartialFunction[Path, Try[String]] = {
    case Root / "move" / idSource / money / idDest => Try {
      money.toDouble
    } match {
      case Success(money) =>
        Try {
          JsonUtil.toJson(ds.transfer(idSource, idDest, money))
        }
      case Failure(reason) => throw reason // It is InternalServerError, should be BadRequest?
    }
    case Root / "add" / idDest / money => Try {
      money.toDouble
    } match {
      case Success(money) =>
        Try {
          JsonUtil.toJson(ds.addMoney(idDest, money))
        }
      case Failure(reason) => throw reason
    }
  }
}
