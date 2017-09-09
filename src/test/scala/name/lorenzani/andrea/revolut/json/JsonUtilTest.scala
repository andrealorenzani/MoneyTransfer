package name.lorenzani.andrea.revolut.json

import name.lorenzani.andrea.revolut.datastore.{Size, User}
import org.scalatest.{FlatSpec, Matchers}

// No need of a Red Phase - I copied this class, I guess I have just to test it
class JsonUtilTest extends FlatSpec with Matchers {

  "JsonUtil" should "convert maps into json and json into maps" in {
    val map = Map[String, String]("a" -> "b", "c" -> "d")
    val json = JsonUtil.toJson(map)
    json should be("{\"a\":\"b\",\"c\":\"d\"}")
    val reconverted = JsonUtil.fromJson[Map[String, String]](json)
    reconverted.get("a") should be(Some("b"))
    reconverted.get("c") should be(Some("d"))
    reconverted.size should be(2)
  }

  it should "convert case classes into json and json into case classes" in {
    val size = Size(3)
    var json = JsonUtil.toJson(size)
    json should be("{\"size\":3}")
    val sizeReconverted = JsonUtil.fromJson[Size](json)
    sizeReconverted.size should be(3)

    val user = User("a", "b", 1)
    json = JsonUtil.toJson(user)
    json should be("{\"id\":\"a\",\"name\":\"b\",\"availability\":1.0}")
    JsonUtil.fromJson[User](json) should be(user)
  }

  it should "consider lists, empty lists and null values" in {
    val userList = List[User]()
    var json = JsonUtil.toJson(userList)
    json should be("[]")
    JsonUtil.fromJson[List[User]](json) should be(userList)

    JsonUtil.toJson(None) should be("{}")
    JsonUtil.toJson(null) should be("{}")
  }
}