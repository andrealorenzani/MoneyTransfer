package name.lorenzani.andrea.revolut.datastore

import scala.collection.mutable

class SimpleMapStore(store: mutable.Map[String, User]) extends DataStore {

  override def add(newUser: User): User = {
    if (store.contains(newUser.id)) throw new IllegalArgumentException(s"ID ${newUser.id} already used")
    store += (newUser.id -> newUser)
    store(newUser.id)
  }

  override def transfer(idSource: String, idDest: String, moneyQty: Double): List[User] = store.synchronized {
    val userSource = getUser(idSource) match {
      case Some(user) if user.availability >= moneyQty => user.copy(availability = user.availability - moneyQty)
      case Some(user) => throw new IllegalArgumentException(s"User ${user.name} has not enough money available")
      case None => throw new IllegalArgumentException(s"Account id $idSource does not exist")
    }
    val userDest = getUser(idDest) match {
      case Some(user) => user.copy(availability = user.availability + moneyQty)
      case None => throw new IllegalArgumentException(s"Account id $idDest does not exist")
    }
    store.put(userSource.id, userSource)
    store.put(userDest.id, userDest)
    List(userSource, userDest)
  }

  override def getUser(id: String): Option[User] = store.get(id)

  override def addMoney(idDest: String, moneyQty: Double): User = store.synchronized {
    val newuser = getUser(idDest) match {
      // Please note: this should be for test only, I didn't check if the availability
      // goes under zero
      case Some(user) => user.copy(availability = user.availability + moneyQty)
      case None => throw new IllegalArgumentException(s"Account id $idDest does not exist")
    }
    store.put(newuser.id, newuser)
    newuser
  }

  override def getAll: List[User] = store.values.toList

  override def size: Int = store.size
}