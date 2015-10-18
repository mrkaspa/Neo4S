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
  implicit val myRelSeqMapper = Mapper.build[MyRelSeq]
}

object UserNodes {

  import UserMappers._

  implicit val userNode = NeoNode("user", "id", (user: MyUser) => user.id)

  implicit val userOptNode = NeoNode("user", "id", (user: MyUserOpt) => user.id.getOrElse(""))

  implicit val userExpNode = NeoNode("user", "id", (user: MyUserExp) => user.id)

}

object UserRels {

  import UserMappers._
  import UserNodes._

  implicit val userRel = NeoRel[MyRel, MyUser, MyUser]("friendship", true)

  implicit val userRelSeq = NeoRel[MyRelSeq, MyUser, MyUser]("friendship", true)
}

case class MyUser(id: String, name: String, age: Int)

case class MyUserOpt(id: Option[String], name: String, age: Option[Int])

case class MyUserExp(id: String, name: String, email: String)

case class MyRel(from: MyUser, to: MyUser, enabled: Boolean) extends Rel[MyUser, MyUser]

case class MyRelSeq(from: MyUser, to: MyUser, enabled: Boolean, loc: Seq[Double]) extends Rel[MyUser, MyUser]


