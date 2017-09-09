package name.lorenzani.andrea.revolut.services

import com.twitter.finagle.http._
import com.twitter.util.Await
import name.lorenzani.andrea.revolut.datastore.{DataStore, User}
import name.lorenzani.andrea.revolut.json.JsonUtil
import org.mockito.Mockito.{mock, _}
import org.scalatest.{FlatSpec, Matchers}

class TransferHandlerTest extends FlatSpec with Matchers {

  private val dataStore = mock(classOf[DataStore])
  private var underTest = new TransferHandler(dataStore)

  behavior of "TransferHandlerTest"

  it should "serve requests for PUT with movement between two users" in {
    val retList = List(User("ids", "", 0), User("idt", "", 100))
    when(dataStore.transfer("ids", "idt", 100)).thenReturn(retList)
    val res = underTest.handle(Request(Method.Put, "/move/ids/100/idt"))
    val cont = JsonUtil.fromJson[List[User]](Await.result(res).contentString)
    verify(dataStore, times(1)).transfer("ids", "idt", 100)
    cont should be(retList)
  }

  it should "serve requests for PUT with add" in {
    val retUsr = User("idt", "", 100)
    when(dataStore.addMoney("idt", 100)).thenReturn(retUsr)
    val res = underTest.handle(Request(Method.Put, "/add/idt/100"))
    val cont = JsonUtil.fromJson[User](Await.result(res).contentString)
    verify(dataStore, times(1)).addMoney("idt", 100)
    cont should be(retUsr)
  }

  it should "not serve strange requests" in {
    val res = underTest.handle(Request(Method.Put, "/add/100"))
    Await.result(res).status should be(Status.NotFound)
    val res2 = underTest.handle(Request(Method.Put, "/move/100"))
    Await.result(res2).status should be(Status.NotFound)
    val res3 = underTest.handle(Request(Method.Put, "/move//100/p"))
    Await.result(res3).status should be(Status.NotFound)
  }

  it should "handle negative availability" in {
    when(dataStore.transfer("idne", "id", 100)).thenThrow(new IllegalArgumentException("Booom"))
    val futres = underTest.handle(Request(Method.Put, "/move/idne/100/id"))
    val res = Await.result(futres)
    res.status should be(Status.BadRequest)
    res.contentString should be("Booom")
  }

}
