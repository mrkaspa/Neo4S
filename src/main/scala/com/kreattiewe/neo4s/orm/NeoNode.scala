package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable
import org.anormcypher.CypherParser._
import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success}

/**
 * Created by michelperez on 4/26/15.
 *
 * every node must be recognizable through an id
 */
abstract class NeoNode[T: Mapper] extends Labelable {

  val MapperT = implicitly[Mapper[T]]

  /** Unwraps the id if this comes in Option */
  def id(t: T): String

  def save(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {

    if (id(t).isEmpty) false
    else {
      val query =
        s"create (n:${label} {props})".stripMargin

      Cypher(query).on("props" -> MapperT.caseToMap(t)).execute()
    }
  }

  /** updates a node of type T looking for t.id */
  def update(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
        match (n:${label} { id: "${id(t)}"})
        set n += {props}
        """.stripMargin
    Cypher(query).on("props" -> MapperT.caseToMap(t)).execute()
  }

  /** deletes a node of type T looking for t.id */
  def delete(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""match (n:${label} { id: "${id(t)}"}) delete n""".stripMargin
    Cypher(query).execute()
  }

  /** deletes a node of type T looking for t.id with its incomming and outgoing relations */
  def deleteWithRelations(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""match (n:${label} { id: "${id(t)}"})-[r]-() delete r, n""".stripMargin
    Cypher(query).execute()
  }

  /** returns the first node looked by the id of type A and labels */
  def findById(id: String, label: Option[String] = None)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Option[T]] = {
    val promisedOption = Promise[Option[T]]()
    Future {
      val labelStr = if (label.isEmpty) "" else s":${label.get}"
      val query =
        s"""match (n$labelStr { id: "$id"}) return n""".stripMargin
      val resOptTry = Cypher(query).as(get[org.anormcypher.NeoNode]("n") *).collectFirst({ case n => n }).map({ case n => NeoQuery.transform(n, MapperT) })
      resOptTry match {
        case Some(resTry) =>
          resTry match {
            case Success(res) => promisedOption.success(Some(res))
            case Failure(e) => promisedOption.failure(e)
          }
        case None => promisedOption.success(None)
      }
    }
    promisedOption.future
  }

  def operations(t: T) = new NeoNodeOperations(t)

  class NeoNodeOperations(t: T) {

    def save()(implicit connection: Neo4jREST, ec: ExecutionContext) = NeoNode.this.save(t)

    def update()(implicit connection: Neo4jREST, ec: ExecutionContext) = NeoNode.this.update(t)

    def delete()(implicit connection: Neo4jREST, ec: ExecutionContext) = NeoNode.this.delete(t)

    def deleteWithRelations()(implicit connection: Neo4jREST, ec: ExecutionContext) = NeoNode.this.deleteWithRelations(t)

  }

}

object NeoNode {

  def apply[T: Mapper](labelS: String, f: T => String) = new NeoNode[T] {
    override def id(t: T): String = f(t)

    override val label: String = labelS
  }

}