package com.kreattiewe.neo4s.example

import com.kreattiewe.neo4s.orm.{Mapper, NeoNode, NeoRel, Rel}
import org.anormcypher.Neo4jREST

import scala.concurrent.Await
import scala.concurrent.duration._

//this import must go
import com.kreattiewe.mapper.macros.Mappable

/**
 * Created by michelperez on 10/17/15.
 */
object Main extends App {

  import com.kreattiewe.neo4s.orm.NeoNodeOperations._
  import com.kreattiewe.neo4s.orm.NeoRelOperations._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class User(name: String, email: String)

  case class Friendship(from: User, to: User, active: Boolean) extends Rel[User, User]

  implicit val userMapper = Mapper.build[User]
  implicit val friendshipMapper = Mapper.build[Friendship]

  implicit val neoUser = NeoNode("user", "email", (user: User) => user.email)
  implicit val neoRel = NeoRel[Friendship, User, User]("friendship", true)

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/", "neo4j", "jokalive")

  val user = User("Michel Perez", "michel.ingesoft@gmail.com")
  val friend = User("Juan David", "juda@hotmail.com")

  val fut = for {
    _ <- user.save()
    _ <- friend.save()
    saved <- Friendship(user, friend, true).save()
  } yield saved

  val saved = Await.result(fut, 1 second)

  println(saved)

}