package name.lorenzani.andrea.revolut.services

import com.twitter.finagle.http.{Method, Request}
import com.twitter.util.Await
import name.lorenzani.andrea.revolut.datastore.{DataStore, User}
import name.lorenzani.andrea.revolut.json.JsonUtil
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.{FlatSpec, Matchers}

class UserHandlerTest extends FlatSpec with Matchers {

  private val dataStore = mock(classOf[DataStore])
  private var underTest = new UserHandler(dataStore)

  behavior of "UserHandlerTest"

  it should "serve requests for GET to retrieve data for a single user" in {
    val retUser = User("ids", "", 0)
    when(dataStore.getUser("ids")).thenReturn(Some(retUser))
    val res = underTest.handle(Request(Method.Get, "/user/ids"))
    val cont = JsonUtil.fromJson[User](Await.result(res).contentString)
    verify(dataStore, times(1)).getUser("ids")
    cont should be(retUser)
  }

  // The full UserHandler is not required by the code test
  // I didn't cover the test for it for saving time
  // I may go back to that after the implementation, if needed

}
