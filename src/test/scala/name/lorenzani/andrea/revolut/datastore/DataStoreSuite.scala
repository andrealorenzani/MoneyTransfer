package name.lorenzani.andrea.revolut.datastore

import java.util.concurrent.{CountDownLatch, Executors}

class DataStoreSuite extends munit.FunSuite:

  private val store = FunFixture[InMemoryDataStore](_ => InMemoryDataStore(), _ => ())

  store.test("adds and retrieves users"): s =>
    val user = User("a", "b", 300)
    assertEquals(s.add(user), Right(user))
    assertEquals(s.size, 1)
    assertEquals(s.get("a"), Some(user))

  store.test("does not retrieve unknown users"): s =>
    assertEquals(s.size, 0)
    assertEquals(s.get("a"), None)

  store.test("keeps users in insertion order"): s =>
    val users = List(User("z", "n1", 1), User("a", "n2", 2), User("m", "n3", 3))
    users.foreach(s.add)
    assertEquals(s.all, users)

  store.test("rejects duplicated ids"): s =>
    s.add(User("a", "b", 0))
    assertEquals(s.add(User("a", "c", 5)), Left(StoreError.UserExists("a")))
    assertEquals(s.get("a").map(_.name), Some("b"))

  store.test("adds money"): s =>
    s.add(User("a", "b", 0))
    assertEquals(s.addMoney("a", 100), Right(User("a", "b", 100)))
    assertEquals(s.get("a").map(_.availability), Some(BigDecimal(100)))

  store.test("rejects non positive amounts and unknown accounts when adding money"): s =>
    s.add(User("a", "b", 10))
    assertEquals(s.addMoney("a", -5), Left(StoreError.InvalidAmount(-5)))
    assertEquals(s.addMoney("a", 0), Left(StoreError.InvalidAmount(0)))
    assertEquals(s.addMoney("nope", 5), Left(StoreError.UnknownAccount("nope")))
    assertEquals(s.get("a").map(_.availability), Some(BigDecimal(10)))

  store.test("transfers money"): s =>
    s.add(User("a", "b", 300))
    s.add(User("c", "d", 0))
    assertEquals(s.transfer("a", "c", 200), Right(List(User("a", "b", 100), User("c", "d", 200))))
    assertEquals(s.get("a").map(_.availability), Some(BigDecimal(100)))
    assertEquals(s.get("c").map(_.availability), Some(BigDecimal(200)))

  store.test("handles fractional amounts exactly"): s =>
    s.add(User("a", "b", BigDecimal("0.3")))
    s.add(User("c", "d", BigDecimal("0.1")))
    s.transfer("a", "c", BigDecimal("0.1"))
    assertEquals(s.get("a").map(_.availability), Some(BigDecimal("0.2")))
    assertEquals(s.get("c").map(_.availability), Some(BigDecimal("0.2")))

  store.test("does not transfer more than the available money"): s =>
    s.add(User("a", "b", 500))
    s.add(User("c", "d", 0))
    assertEquals(s.transfer("a", "c", 10000), Left(StoreError.InsufficientFunds("b")))
    assertEquals(s.get("a").map(_.availability), Some(BigDecimal(500)))
    assertEquals(s.get("c").map(_.availability), Some(BigDecimal(0)))

  store.test("does not transfer to or from unknown accounts, nor to itself"): s =>
    s.add(User("a", "b", 500))
    assertEquals(s.transfer("a", "x", 10), Left(StoreError.UnknownAccount("x")))
    assertEquals(s.transfer("x", "a", 10), Left(StoreError.UnknownAccount("x")))
    assertEquals(s.transfer("a", "a", 10), Left(StoreError.SameAccount("a")))
    assertEquals(s.get("a").map(_.availability), Some(BigDecimal(500)))

  store.test("keeps the total amount of money under concurrent transfers"): s =>
    s.add(User("a", "b", 300))
    s.add(User("c", "d", 300))
    val pool = Executors.newFixedThreadPool(8)
    val done = CountDownLatch(200)
    for i <- 0 until 200 do
      pool.execute: () =>
        if i % 2 == 0 then s.transfer("a", "c", 3) else s.transfer("c", "a", 3)
        done.countDown()
    done.await()
    pool.shutdown()
    assertEquals(s.all.map(_.availability).sum, BigDecimal(600))
    assertEquals(s.get("a").map(_.availability), Some(BigDecimal(300)))
