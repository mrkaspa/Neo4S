package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable
import org.anormcypher.CypherParser._
import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by michelperez on 4/25/15.
 *
 * extend it to create a dao of type T
 */
trait NodeDAO[T <: NeoNode[A], A] {

  object Mapper extends Mapper[T]

  /** saves a node of type T */
  def save[T <: NeoNode[A] : Mappable](t: T)(implicit connection: Neo4jREST): Future[Boolean] = {
    val query =
      s"create (n${t.labelsString()} {props})".stripMargin
    Future {
      Cypher(query).on("props" -> Mapper.caseToMap(t)).execute()
    }
  }

  /** updates a node of type T looking for t.id */
  def update[T <: NeoNode[A] : Mappable](t: T)(implicit connection: Neo4jREST): Future[Boolean] = {
    val query =
      s"""
        match (n${t.labelsString()} { id: "${t.id}"})
        set n = {props}
        """.stripMargin
    Future {
      Cypher(query).on("props" -> Mapper.caseToMap(t)).execute()
    }
  }

  /** deletes a node of type T looking for t.id */
  def delete[T <: NeoNode[A]](t: T)(implicit connection: Neo4jREST): Future[Boolean] = {
    val query =
      s"""match (n${t.labelsString()} { id: "${t.id}"}) delete n""".stripMargin
    Future {
      Cypher(query).execute()
    }
  }

  /** returns the first node looked by the id of type A and labels */
  def findById[T <: NeoNode[A] : Mappable](a: A, labelsOpt: Option[Seq[String]] = None)(implicit connection: Neo4jREST): Future[Option[T]] = {
    val labels = labelsOpt.getOrElse(Seq[String]()).map(x => ":" + x)
    val query =
      s"""match (n ${labels}{ id: "${a}"}) return n""".stripMargin
    Future {
      Cypher(query).as(get[org.anormcypher.NeoNode]("n") *).collectFirst({ case n => n }).map({ case n => Mapper.mapToCase[T](n.props) })
    }
  }

}
