package name.lorenzani.andrea.revolut

import com.twitter.finagle.Http
import com.twitter.util.Await
import name.lorenzani.andrea.revolut.services.RequestHandler._
import name.lorenzani.andrea.revolut.services.RestService

object MoneyTransfer extends App {
  val restApi = new RestService(getTransferHandler, getUserHandler)
  val server = Http.server
    .withLabel("MoneyTransfer")
    .serve(":8080", restApi)
  Await.ready(server) // waits until the server resources are released
}
