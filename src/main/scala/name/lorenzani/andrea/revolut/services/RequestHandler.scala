package name.lorenzani.andrea.revolut.services

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response, Status, Version}
import com.twitter.util.Future
import name.lorenzani.andrea.revolut.datastore.DataStore

import scala.util.{Failure, Success, Try}

trait RequestHandler {
  def handle(request: Request): Future[Response] = {
    if (route(request).isDefinedAt(Path(request.path))) {
      Future {
        val response = Response()
        route(request).apply(Path(request.path)) match {
          case Success(res) => {
            response.setContentTypeJson()
            response.contentString = res
          }
          case Failure(ex) => {
            println(s"Error occurred while handling request to '${request.path}': ${ex.getMessage}")
            response.status = Status.BadRequest
            response.contentString = ex.getMessage
          }
        }
        response
      }
    }
    else {
      Future value Response(Version.Http11, Status.NotFound)
    }
  }

  def route(request: Request): PartialFunction[Path, Try[String]]
}

object RequestHandler {

  def getTransferHandler: RequestHandler = new TransferHandler(DataStore.getDataStore)

  def getUserHandler: RequestHandler = new UserHandler(DataStore.getDataStore)

}
