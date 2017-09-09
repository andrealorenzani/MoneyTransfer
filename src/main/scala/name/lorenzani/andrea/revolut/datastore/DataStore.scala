package name.lorenzani.andrea.revolut.datastore

import scala.collection.mutable

sealed case class User(id: String, name: String, availability: Double)

sealed case class Size(size: Int)

abstract class DataStore {
  def getUser(id: String): Option[User]
  def add(newUser: User): User
  def transfer(idSource: String, idDest: String, moneyQty: Double): List[User]
  def addMoney(idDest: String, moneyQty: Double): User
  def getAll: List[User]
  def size: Int
}

object DataStore {
  private val map = mutable.Map[String, User]()
  private val ds = new SimpleMapStore(map)

  def getDataStore: DataStore = ds
}