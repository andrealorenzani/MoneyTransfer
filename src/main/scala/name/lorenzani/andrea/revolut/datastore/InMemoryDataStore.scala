package name.lorenzani.andrea.revolut.datastore

import scala.collection.immutable.VectorMap

/** Thread-safe store: reads see an immutable snapshot, writes are serialised so transfers are atomic. */
class InMemoryDataStore extends DataStore:

  @volatile private var users = VectorMap.empty[String, User]

  override def get(id: String): Option[User] = users.get(id)

  override def add(newUser: User): Either[StoreError, User] = synchronized:
    if users.contains(newUser.id) then Left(StoreError.UserExists(newUser.id))
    else
      users += newUser.id -> newUser
      Right(newUser)

  override def addMoney(id: String, amount: BigDecimal): Either[StoreError, User] = synchronized:
    for
      _ <- positive(amount)
      user <- find(id)
    yield save(user.copy(availability = user.availability + amount))

  override def transfer(idSource: String, idDest: String, amount: BigDecimal): Either[StoreError, List[User]] =
    synchronized:
      for
        _ <- positive(amount)
        _ <- Either.cond(idSource != idDest, (), StoreError.SameAccount(idSource))
        source <- find(idSource)
        _ <- Either.cond(source.availability >= amount, (), StoreError.InsufficientFunds(source.name))
        dest <- find(idDest)
      yield List(
        save(source.copy(availability = source.availability - amount)),
        save(dest.copy(availability = dest.availability + amount))
      )

  override def all: List[User] = users.values.toList

  override def size: Int = users.size

  private def find(id: String): Either[StoreError, User] = users.get(id).toRight(StoreError.UnknownAccount(id))

  private def positive(amount: BigDecimal): Either[StoreError, Unit] =
    Either.cond(amount > 0, (), StoreError.InvalidAmount(amount))

  private def save(user: User): User =
    users += user.id -> user
    user
