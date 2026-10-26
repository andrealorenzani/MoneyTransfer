package name.lorenzani.andrea.revolut.services

import name.lorenzani.andrea.revolut.datastore.{InMemoryDataStore, Size, User}
import name.lorenzani.andrea.revolut.json.Json
import name.lorenzani.andrea.revolut.json.Json.given

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

/** End-to-end scenarios over real HTTP (they used to be the Cucumber feature file). */
class RestServerSuite extends munit.FunSuite:

  private val running = FunFixture[(RestServer, HttpClient)](
    _ => (RestServer(Api(InMemoryDataStore()), 0).start(), HttpClient.newHttpClient()),
    (server, client) =>
      server.close()
      client.close()
  )

  private def send(server: RestServer, client: HttpClient, method: String, path: String) =
    val request = HttpRequest
      .newBuilder(URI.create(s"http://localhost:${server.boundPort}$path"))
      .method(method, HttpRequest.BodyPublishers.noBody())
      .build()
    client.send(request, HttpResponse.BodyHandlers.ofString())

  running.test("adds two users and transfers money"): (server, client) =>
    def call(method: String, path: String) = send(server, client, method, path)
    call("POST", "/user/Thomas/id1")
    call("PUT", "/add/id1/500")
    call("POST", "/user/Brunone/id2")
    assertEquals(call("PUT", "/move/id1/100/id2").statusCode, 200)
    assertEquals(Json.fromJson[Size](call("GET", "/size").body).size, 2)
    assertEquals(Json.fromJson[User](call("GET", "/user/id1").body).availability, BigDecimal(400))
    assertEquals(Json.fromJson[User](call("GET", "/user/id2").body).availability, BigDecimal(100))

  running.test("fails to transfer more money than available"): (server, client) =>
    send(server, client, "POST", "/user/Brunone/id3")
    send(server, client, "POST", "/user/Thomas/id4")
    send(server, client, "PUT", "/add/id3/100")
    val res = send(server, client, "PUT", "/move/id3/200/id4")
    assertEquals(res.statusCode, 400)
    assertEquals(res.body, "User Brunone has not enough money available")

  running.test("answers with a json content type and 404 on unknown paths"): (server, client) =>
    assertEquals(send(server, client, "GET", "/size").headers.firstValue("Content-Type").get, "application/json")
    assertEquals(send(server, client, "GET", "/nope").statusCode, 404)
