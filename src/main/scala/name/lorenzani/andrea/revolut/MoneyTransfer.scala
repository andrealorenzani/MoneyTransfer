package name.lorenzani.andrea.revolut

import name.lorenzani.andrea.revolut.datastore.InMemoryDataStore
import name.lorenzani.andrea.revolut.services.{Api, RestServer}

import java.util.concurrent.CountDownLatch

@main def moneyTransfer(): Unit =
  val port = sys.env.get("PORT").flatMap(_.toIntOption).getOrElse(8080)
  val server = RestServer(Api(InMemoryDataStore()), port).start()
  println(s"MoneyTransfer listening on port ${server.boundPort}")
  CountDownLatch(1).await() // serve until the process is killed
