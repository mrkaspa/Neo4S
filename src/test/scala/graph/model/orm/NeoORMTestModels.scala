package graph.model.orm

import com.kreattiewe.neo4s.orm._
import com.kreattiewe.mapper.macros.Mappable

/**
 * Created by michelperez on 4/26/15.
 */
object UserMappers {
  implicit val myUserMapper = Mapper.build[MyUser]
  implicit val myUserOptMapper = Mapper.build[MyUserOpt]
  implicit val myUserExpMapper = Mapper.build[MyUserExp]
  implicit val myRelMapper = Mapper.build[MyRel]
}

import UserMappers._

case class MyUser(id: String, name: String, age: Int) extends NeoNode[String] {
  override val label = "user"
}

object MyUserDAO extends NodeDAO[MyUser, String]


case class MyRel(from: MyUser, to: MyUser, enabled: Boolean) extends NeoRel[MyUser, MyUser] {
  override val label = "friendship"
}

object MyRelDAO extends RelDAO[MyUser, MyUser, MyRel]

case class MyUserOpt(id: Option[String], name: String, age: Option[Int]) extends NeoNode[Option[String]] {
  override val label = "user"
}

object MyUserOptDAO extends NodeDAO[MyUserOpt, Option[String]]

case class MyUserExp(id: String, name: String, email: String) extends NeoNode[String] {
  override val label = "user"
}

object MyUserExpDAO extends NodeDAO[MyUserExp, String]