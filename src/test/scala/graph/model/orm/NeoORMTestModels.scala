package graph.model.orm

import com.kreattiewe.neo4s.orm._
import com.kreattiewe.mapper.macros.Mappable

/**
 * Created by michelperez on 4/26/15.
 */
object UserMappers {
  implicit val myUserMapper = Mapper.build[MyUser]
  //  implicit val myUserOptMapper = Mapper.build[MyUserOpt]
  //  implicit val myUserExpMapper = Mapper.build[MyUserExp]
  implicit val myRelMapper = Mapper.build[MyRel]
  //  implicit val myRelSeqMapper = Mapper.build[MyRelSeq]
}

object UserNodes {

  import UserMappers._

  implicit val userNode = NeoNode("user", (user: MyUser) => user.id)

  implicit def parseUserNode(user: MyUser) = NeoNodeOperations(user)

}

object UserRels {

  import UserMappers._
  import UserNodes._

  val userRel = NeoRel[MyRel, MyUser, MyUser]("friendship", true)

  implicit def parseUserRel(rel: MyRel) = NeoRelOperations[MyRel, MyUser, MyUser](userRel, rel)
}

//object Rels{
//  implicit val userUserRels = NeoRel.newRel[User]("friendship")
//}

import UserNodes._
import UserMappers._

case class MyUser(id: String, name: String, age: Int)

//object MyUserDAO extends NodeDAO[MyUser, String]


case class MyRel(from: MyUser, to: MyUser, enabled: Boolean) extends Rel[MyUser, MyUser]

//
//case class MyRelSeq(from: MyUser, to: MyUser, enabled: Boolean, loc: Seq[Double]) extends NeoRel[MyUser, MyUser] {
//  override val label = "friendship_seq"
//}
//
//object MyRelDAO extends RelDAO[MyUser, MyUser, MyRel]
//
//object MyRelSeqDAO extends RelDAO[MyUser, MyUser, MyRelSeq]
//
//case class MyUserOpt(id: Option[String], name: String, age: Option[Int]) extends NeoNode[Option[String]] {
//  override val label = "user"
//}
//
//object MyUserOptDAO extends NodeDAO[MyUserOpt, Option[String]]
//
//case class MyUserExp(id: String, name: String, email: String) extends NeoNode[String] {
//  override val label = "user"
//}
//
//object MyUserExpDAO extends NodeDAO[MyUserExp, String]