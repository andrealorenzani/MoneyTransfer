package steps

import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future => FinagleFuture}
import cucumber.api.PendingException
import cucumber.api.scala.{EN, ScalaDsl}
import name.lorenzani.andrea.revolut.MoneyTransfer
import name.lorenzani.andrea.revolut.datastore.{Size, User}
import name.lorenzani.andrea.revolut.json.JsonUtil
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class StepsDefinition extends ScalaDsl with EN {

  val undertest = Future { MoneyTransfer.main(new Array[String](0)) }

  Given("""^MoneyTransfer is running$"""){ () =>
  }

  When("""^I add user (\w+) with name (\w+) and availability (\d+)$"""){ (id:String, name:String, money:Int) =>
    sendRequest(s"/user/$name/$id", http.Method.Post)
    sendRequest(s"/add/$id/$money", http.Method.Put)
  }
  When("""^I transfer (\d+) money from (\w+) to (\w+)$"""){ (money:Int, idS:String, idD:String) =>
    sendRequest(s"/move/$idS/$money/$idD", http.Method.Put)
  }
  Then("""^I fail transfering (\d+) money from (\w+) to (\w+) because (\w+) has not nough money$"""){
    (money:Int, idS:String, idD:String, name: String) =>
      val reply = sendRequest(s"/move/$idS/$money/$idD", http.Method.Put)
      assert(reply.status == http.Status.BadRequest)
      assert(s"User $name has not enough money available" == reply.contentString)
  }
  Then("""^it replies to size with (\d+)$"""){ (expectedSize:Int) =>
    val response = sendRequest("/size", http.Method.Get)
    val size = JsonUtil.fromJson[Size](response.contentString)
    assert(size.size == expectedSize)
  }
  Then("""^it shows that (\w+) has availability of (\d+)$"""){ (id:String, expectedMoney:Int) =>
    val avail = sendRequest(s"/user/$id", http.Method.Get)
    val userData = JsonUtil.fromJson[User](avail.contentString)
    assert(userData.availability == expectedMoney)
  }

  def sendRequest(url: String, method: http.Method): http.Response = {
    val client: Service[http.Request, http.Response] = Http.newService("localhost:8080")
    val request = http.Request(method, url)
    request.host = "localhost:8080"
    val response: FinagleFuture[http.Response] = client(request)
    Await.result(response)
  }

}
