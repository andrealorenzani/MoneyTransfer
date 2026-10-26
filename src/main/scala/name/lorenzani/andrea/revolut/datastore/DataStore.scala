package name.lorenzani.andrea.revolut.datastore

final case class User(id: String, name: String, availability: BigDecimal)

final case class Size(size: Int)

enum StoreError(val message: String):
  case UserExists(id: String) extends StoreError(s"ID $id already used")
  case UnknownAccount(id: String) extends StoreError(s"Account id $id does not exist")
  case InsufficientFunds(name: String) extends StoreError(s"User $name has not enough money available")
  case InvalidAmount(amount: BigDecimal) extends StoreError(s"Amount $amount must be greater than zero")
  case SameAccount(id: String) extends StoreError(s"Cannot transfer money from account $id to itself")

trait DataStore:
  def get(id: String): Option[User]
  def add(newUser: User): Either[StoreError, User]
  def addMoney(id: String, amount: BigDecimal): Either[StoreError, User]
  def transfer(idSource: String, idDest: String, amount: BigDecimal): Either[StoreError, List[User]]
  def all: List[User]
  def size: Int
