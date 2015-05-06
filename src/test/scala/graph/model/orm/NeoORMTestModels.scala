package graph.model.orm

import com.kreattiewe.neo4s.orm.{RelDAO, NeoRel, NodeDAO, NeoNode}

/**
 * Created by michelperez on 4/26/15.
 */

case class MyUser(id: String, name: String, age: Int) extends NeoNode[String] {
  override val labels: Set[String] = Set("user")
}

object MyUserDAO extends NodeDAO[MyUser, String]


case class MyRel(from: MyUser, to: MyUser, enabled: Boolean) extends NeoRel[MyUser, MyUser] {
  override val labels: Set[String] = Set("friendship")
}

object MyRelDAO extends RelDAO[MyUser, MyUser, MyRel]

case class MyUserOpt(id: Option[String], name: String, age: Option[Int]) extends NeoNode[Option[String]] {
  override val labels: Set[String] = Set("user")
}

object MyUserOptDAO extends NodeDAO[MyUserOpt, Option[String]]

case class MyUserExp(id: String, name: String, email: String) extends NeoNode[String] {
  override val labels: Set[String] = Set("user")
}

object MyUserExpDAO extends NodeDAO[MyUserExp, String]