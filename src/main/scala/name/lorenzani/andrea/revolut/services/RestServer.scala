package name.lorenzani.andrea.revolut.services

import com.sun.net.httpserver.{HttpExchange, HttpServer}

import java.net.InetSocketAddress
import java.util.concurrent.Executors

/** Exposes an [[Api]] through the HTTP server that ships with the JDK. */
class RestServer(api: Api, port: Int) extends AutoCloseable:

  private val server = HttpServer.create(InetSocketAddress(port), 0)
  server.createContext("/", exchange => respond(exchange))
  server.setExecutor(Executors.newVirtualThreadPerTaskExecutor())

  def start(): RestServer =
    server.start()
    this

  /** The port actually bound, useful when created with port 0. */
  def boundPort: Int = server.getAddress.getPort

  override def close(): Unit = server.stop(0)

  private def respond(exchange: HttpExchange): Unit =
    try
      exchange.getRequestBody.readAllBytes() // the API is driven by the path, bodies are ignored
      val response = api.handle(exchange.getRequestMethod, exchange.getRequestURI.getPath)
      val bytes = response.body.getBytes("UTF-8")
      exchange.getResponseHeaders.set("Content-Type", response.contentType)
      exchange.sendResponseHeaders(response.status, bytes.length)
      exchange.getResponseBody.write(bytes)
    catch
      case e: Exception =>
        System.err.println(s"Error handling ${exchange.getRequestURI}: ${e.getMessage}")
        exchange.sendResponseHeaders(500, -1)
    finally exchange.close()
