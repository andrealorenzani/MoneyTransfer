package name.lorenzani.andrea.revolut.json

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

// Copied from https://coderwall.com/p/o--apg/easy-json-un-marshalling-in-scala-with-jackson

object JsonUtil {
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def toJson(value: Map[Symbol, Any]): String = {
    if (Option(value).isEmpty) "{}"
    else toJson(value map { case (k, v) => k.name -> v })
  }

  def toJson(value: Any): String = value match {
    case _ if Option(value).isEmpty => "{}"
    case None => "{}"
    case concrValue => mapper.writeValueAsString(concrValue)
  }

  def toMap[V](json: String)(implicit m: Manifest[V]): Map[String, V] = fromJson[Map[String, V]](json)

  def fromJson[T](json: String)(implicit m: Manifest[T]): T = {
    mapper.readValue[T](json)
  }
}