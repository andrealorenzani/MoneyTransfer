package name.lorenzani.andrea.revolut.services

import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}

class RestServiceTest extends FlatSpec with Matchers {

  private val succString = "Success"
  private val failString = "Failure"

  private var transferHandler = mock(classOf[TransferHandler])
  private var userHandler = mock(classOf[UserHandler])
  private var succ = Future value Response(Version.Http11, Status.Ok)
  private var failure = Future value Response(Version.Http11, Status.BadRequest)
  private var underTest = new RestService(transferHandler, userHandler)

  behavior of "RestServiceTest"

  it should "serve requests for GET and POST expecting to be served by userHandler" in {
    when(userHandler.handle(any())).thenReturn(succ)
    val resGet = underTest.apply(Request(Method.Get, "/size"))
    resGet should be(succ)
    verify(userHandler, times(1)).handle(any())
    val resPost = underTest.apply(Request(Method.Post, "/user"))
    resPost should be(succ)
    verify(userHandler, times(2)).handle(any())
  }

  it should "serve requests for PUT expecting to be served by transferHandler" in {
    when(transferHandler.handle(any())).thenReturn(succ)
    val resGet = underTest.apply(Request(Method.Put, "/move"))
    resGet should be(succ)
    verify(transferHandler, times(1)).handle(any())
  }

  it should "not serve any other method" in {
    when(transferHandler.handle(any())).thenReturn(failure)
    when(userHandler.handle(any())).thenReturn(failure)

    var res = underTest.apply(Request(Method.Delete, "/move"))
    Await.result(res).status should be(Status.NotFound)
    res = underTest.apply(Request(Method.Connect, "/move"))
    Await.result(res).status should be(Status.NotFound)
    res = underTest.apply(Request(Method.Head, "/move"))
    Await.result(res).status should be(Status.NotFound)
    res = underTest.apply(Request(Method.Options, "/move"))
    Await.result(res).status should be(Status.NotFound)
    res = underTest.apply(Request(Method.Patch, "/move"))
    Await.result(res).status should be(Status.NotFound)
    res = underTest.apply(Request(Method.Trace, "/move"))
    Await.result(res).status should be(Status.NotFound)

  }

}
