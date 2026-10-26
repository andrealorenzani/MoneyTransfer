package name.lorenzani.andrea.revolut.json

import name.lorenzani.andrea.revolut.datastore.{Size, User}
import name.lorenzani.andrea.revolut.json.Json.given

class JsonSuite extends munit.FunSuite:

  test("converts case classes to json and back"):
    assertEquals(Json.toJson(Size(3)), """{"size":3}""")
    val user = User("a", "b", 1)
    assertEquals(Json.toJson(user), """{"id":"a","name":"b","availability":1}""")
    assertEquals(Json.fromJson[User](Json.toJson(user)), user)

  test("keeps decimal amounts exact"):
    val user = User("a", "b", BigDecimal("0.10"))
    assertEquals(Json.fromJson[User](Json.toJson(user)).availability, BigDecimal("0.10"))

  test("converts lists, including empty ones"):
    assertEquals(Json.toJson(List.empty[User]), "[]")
    assertEquals(Json.fromJson[List[User]]("[]"), Nil)
    val users = List(User("a", "b", 1), User("c", "d", 2))
    assertEquals(Json.fromJson[List[User]](Json.toJson(users)), users)

  test("ignores nothing silently: malformed json fails"):
    intercept[com.github.plokhotnyuk.jsoniter_scala.core.JsonReaderException](Json.fromJson[Size]("{"))
