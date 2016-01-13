package com.mrkaspa.neo4s.orm

import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by michelperez on 4/26/15.
 *
 * every node must be recognizable through an id
 */
abstract class NeoNode[T: Mapper] extends Labelable {

  val MapperT = implicitly[Mapper[T]]

  /** Unwraps the id if this comes in Option */
  def id(t: T): String

  val idColumn: String

  def save(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {

    if (id(t).isEmpty) false
    else {
      val query =
        s"create (n:${label} {props}) return n".stripMargin
      val rows = Cypher(query).on("props" -> MapperT.caseToMap(t))().size
      rows > 0
    }
  }

  /** updates a node of type T looking for t.id */
  def update(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
        match (n:${label} { ${idColumn}: "${id(t)}"})
        set n += {props}
        return n
        """.stripMargin
    val rows = Cypher(query).on("props" -> MapperT.caseToMap(t))().size
    rows > 0
  }

  /** deletes a node of type T looking for t.id */
  def delete(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""match (n:${label} { ${idColumn}: "${id(t)}"}) delete n""".stripMargin
    Cypher(query).execute()
  }

  /** deletes a node of type T looking for t.id with its incomming and outgoing relations */
  def deleteWithRelations(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""match (n:${label} { ${idColumn}: "${id(t)}"})-[r]-() delete r, n""".stripMargin
    Cypher(query).execute()
  }

}

/** Creates a NeoNode instance */
object NeoNode {

  def apply[T: Mapper](labelS: String, idColumnVal: String, f: T => String) = new NeoNode[T] {
    override def id(t: T): String = f(t)

    override val label: String = labelS

    override val idColumn: String = idColumnVal
  }

}

/** Converts T into an operations of T */

object NeoNodeOperations {
  implicit def toOperations[T](t: T)(implicit neoNode: NeoNode[T]) = new NeoNodeOperations(t, neoNode)
}

case class NeoNodeOperations[T](t: T, neoNode: NeoNode[T]) {

  /** Calls save on T */
  def save()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoNode.save(t)

  /** Calls update on T */
  def update()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoNode.update(t)

  /** Calls delete on T */
  def delete()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoNode.delete(t)

  /** Calls deleteWithRelations on T */
  def deleteWithRelations()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoNode.deleteWithRelations(t)

}