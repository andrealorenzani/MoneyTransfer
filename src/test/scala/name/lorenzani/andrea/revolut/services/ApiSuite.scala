package name.lorenzani.andrea.revolut.services

import name.lorenzani.andrea.revolut.datastore.{InMemoryDataStore, Size, User}
import name.lorenzani.andrea.revolut.json.Json
import name.lorenzani.andrea.revolut.json.Json.given

class ApiSuite extends munit.FunSuite:

  private val api = FunFixture[Api](_ => Api(InMemoryDataStore()), _ => ())

  api.test("creates and shows a user"): api =>
    val created = api.handle("POST", "/user/Thomas/id1")
    assertEquals(created.status, 201)
    assertEquals(Json.fromJson[User](created.body), User("id1", "Thomas", 0))
    val shown = api.handle("GET", "/user/id1")
    assertEquals(shown.status, 200)
    assertEquals(shown.contentType, "application/json")
    assertEquals(Json.fromJson[User](shown.body), User("id1", "Thomas", 0))

  api.test("generates an id when none is given"): api =>
    assertEquals(Json.fromJson[User](api.handle("POST", "/user/Thomas").body).id, "0")
    assertEquals(Json.fromJson[User](api.handle("POST", "/user/Brunone").body).id, "1")

  api.test("rejects a duplicated id"): api =>
    api.handle("POST", "/user/Thomas/id1")
    val res = api.handle("POST", "/user/Other/id1")
    assertEquals(res.status, 400)
    assertEquals(res.body, "ID id1 already used")

  api.test("lists users and their number"): api =>
    assertEquals(api.handle("GET", "/").body, "[]")
    api.handle("POST", "/user/Thomas/id1")
    assertEquals(Json.fromJson[List[User]](api.handle("GET", "/").body), List(User("id1", "Thomas", 0)))
    assertEquals(Json.fromJson[Size](api.handle("GET", "/size").body), Size(1))

  api.test("answers 404 for unknown users"): api =>
    assertEquals(api.handle("GET", "/user/nobody").status, 404)

  api.test("adds money"): api =>
    api.handle("POST", "/user/Thomas/id1")
    val res = api.handle("PUT", "/add/id1/100.50")
    assertEquals(res.status, 200)
    assertEquals(Json.fromJson[User](res.body).availability, BigDecimal("100.50"))

  api.test("transfers money"): api =>
    api.handle("POST", "/user/Thomas/id1")
    api.handle("POST", "/user/Brunone/id2")
    api.handle("PUT", "/add/id1/500")
    val res = api.handle("PUT", "/move/id1/100/id2")
    assertEquals(res.status, 200)
    assertEquals(
      Json.fromJson[List[User]](res.body),
      List(User("id1", "Thomas", 400), User("id2", "Brunone", 100))
    )

  api.test("explains why a transfer fails"): api =>
    api.handle("POST", "/user/Brunone/id3")
    api.handle("POST", "/user/Thomas/id4")
    api.handle("PUT", "/add/id3/100")
    val res = api.handle("PUT", "/move/id3/200/id4")
    assertEquals(res.status, 400)
    assertEquals(res.body, "User Brunone has not enough money available")

  api.test("rejects invalid amounts with 400"): api =>
    api.handle("POST", "/user/Thomas/id1")
    assertEquals(api.handle("PUT", "/add/id1/lots").status, 400)
    assertEquals(api.handle("PUT", "/add/id1/-5").status, 400)
    assertEquals(api.handle("PUT", "/move/id1/abc/id1").status, 400)

  api.test("does not serve strange requests"): api =>
    for (method, path) <- List(
        "PUT" -> "/add/100",
        "PUT" -> "/move/100",
        "PUT" -> "/move//100/p",
        "DELETE" -> "/user/id1",
        "PATCH" -> "/move/a/1/b",
        "GET" -> "/whatever"
      )
    do assertEquals(api.handle(method, path).status, 404, s"$method $path")
