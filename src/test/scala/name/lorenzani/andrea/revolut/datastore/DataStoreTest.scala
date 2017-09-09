package name.lorenzani.andrea.revolut.datastore

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

class DataStoreTest extends FlatSpec with Matchers with BeforeAndAfterEach {

  private var underTest = new SimpleMapStore(new MapWithDelays)

  override def beforeEach(): Unit = {
    underTest = new SimpleMapStore(new MapWithDelays)
  }

  behavior of "RestServiceTest"

  it should "add and retrieve users" in {
    val testUsr = User("a", "b", 300)
    underTest.add(testUsr)
    underTest.size should be(1)
    underTest.getUser("a") should be(Some(testUsr))
  }

  it should "not retrieve user with unknown id" in {
    underTest.size should be(0)
    underTest.getUser("a") should be(None)
  }

  it should "add money to an user" in {
    val testUsr = User("a", "b", 0)
    underTest.add(testUsr)
    underTest.size should be(1)
    underTest.addMoney("a", 100)
    underTest.getUser("a") should be(Some(testUsr.copy(availability = 100)))
  }

  it should "add and retrieve multipleUsers" in {
    val testUsr = User("a", "b", 300)
    val testUsr2 = User("c", "d", 0)
    underTest.add(testUsr)
    underTest.add(testUsr2)
    underTest.size should be(2)
    underTest.getUser("a") should be(Some(testUsr))
    underTest.getUser("c") should be(Some(testUsr2))
    underTest.getAll should be(List(testUsr, testUsr2))
  }

  it should "transfer money" in {
    val testUsr = User("a", "b", 300)
    val testUsr2 = User("c", "d", 0)
    underTest.add(testUsr)
    underTest.add(testUsr2)
    underTest.transfer("a", "c", 200)
    underTest.getUser("a") should be(Some(testUsr.copy(availability = 100)))
    underTest.getUser("c") should be(Some(testUsr2.copy(availability = 200)))
  }

  it should "not create problems in multithreading" in {
    val testUsr = User("a", "b", 300)
    val testUsr2 = User("c", "d", 300)
    underTest.add(testUsr)
    underTest.add(testUsr2)
    val f1 = Future {
      for (i <- 1 to 10) {
        underTest.transfer("a", "c", 3)
      }
    }
    val f2 = Future {
      for (i <- 1 to 10) {
        underTest.transfer("c", "a", 3)
      }
    }
    Await.ready(Future.sequence(List(f1, f2)), Duration.Inf)
    underTest.getUser("a") should be(Some(testUsr))
    underTest.getUser("c") should be(Some(testUsr2))
  }

  it should "not transfer money over availability" in {
    val testUsr = User("a", "b", 500)
    val testUsr2 = User("c", "d", 0)
    underTest.add(testUsr)
    underTest.add(testUsr2)
    Try {
      underTest.transfer("a", "c", 10000)
    }.isFailure
    underTest.getUser("a") should be(Some(testUsr))
    underTest.getUser("c") should be(Some(testUsr2))
  }
}

class MapWithDelays extends mutable.HashMap[String, User] {
  override def put(key: String, value: User): Option[User] = {
    Thread.sleep(50)
    //println(s"Changing $key")
    super.put(key, value)
  }
}
