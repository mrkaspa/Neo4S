package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable
import org.anormcypher.{Neo4jREST, Cypher}
import org.anormcypher.CypherParser._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
 * Created by michelperez on 4/29/15.
 *
 * parses the results of a query
 */
object NeoQuery {

  /** parses the results from a list of NeoNodes to a list of A */
  def transform[A: Mappable](res: List[org.anormcypher.NeoNode]): List[A] = {
    object Mapper extends Mapper[A]
    res.map({ case n => Mapper.mapToCase[A](n.props) })
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transform[A <: NeoNode[_] : Mappable, B <: NeoNode[_] : Mappable, C <: NeoRel[A, B] : Mappable](res: List[(org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship)]): List[C] = {
    object MapperA extends Mapper[A]
    object MapperB extends Mapper[B]
    object MapperC extends Mapper[C]

    res.map({
      case (a, b, c) =>
        val from = MapperA.mapToCase[A](a.props)
        val to = MapperB.mapToCase[B](b.props)
        val props = c.props +("to" -> to, "from" -> from)
        MapperC.mapToCase[C](props)
    })
  }

  /** Execute a query that returns a List[A] */
  def executeQuery[A: Mappable](query: String)(implicit connection: Neo4jREST): Future[List[A]] = {
    Future {
      val res = Cypher(query).as(get[org.anormcypher.NeoNode]("a") *)
      transform[A](res)
    }
  }

  /** Execute a query that returns a List[A] */
  def executeQuery[A <: NeoNode[_] : Mappable, B <: NeoNode[_] : Mappable, C <: NeoRel[A, B] : Mappable](query: String)(implicit connection: Neo4jREST): Future[List[C]] = {
    Future {
      val res = Cypher(query).as(get[org.anormcypher.NeoNode]("a") ~ get[org.anormcypher.NeoNode]("b") ~ get[org.anormcypher.NeoRelationship]("c") *).map(flatten)
      transform[A, B, C](res)
    }
  }

}
