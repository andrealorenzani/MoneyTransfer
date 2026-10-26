package name.lorenzani.andrea.revolut.json

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import name.lorenzani.andrea.revolut.datastore.{Size, User}

object Json:
  given JsonValueCodec[User] = JsonCodecMaker.make
  given JsonValueCodec[Size] = JsonCodecMaker.make
  given JsonValueCodec[List[User]] = JsonCodecMaker.make

  def toJson[A: JsonValueCodec](value: A): String = writeToString(value)

  def fromJson[A: JsonValueCodec](json: String): A = readFromString[A](json)
