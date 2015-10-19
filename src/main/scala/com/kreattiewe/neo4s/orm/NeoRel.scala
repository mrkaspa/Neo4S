package com.kreattiewe.neo4s.orm

import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by michelperez on 4/27/15.
 *
 * every relationship has and start and end point
 */

/** Every relationship must inherit this */
abstract class Rel[A, B] {
  val from: A
  val to: B
}

abstract class NeoRel[C <: Rel[A, B] : Mapper, A: NeoNode, B: NeoNode] extends Labelable {

  val MapperC = implicitly[Mapper[C]]
  val NeoNodeA = implicitly[NeoNode[A]]
  val NeoNodeB = implicitly[NeoNode[B]]

  val unique: Boolean

  def validateUniqueness(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean]
  = Future {
    val query =
      s"""
         match (a:${NeoNodeA.label}{ ${NeoNodeA.idColumn}: "${NeoNodeA.id(c.from)}"})-[c:${label}]->(b:${NeoNodeB.label}{ ${NeoNodeB.idColumn}: "${NeoNodeB.id(c.to)}"})
         return c
       """.stripMargin
    val rows = Cypher(query)().size
    rows == 0
  }


  /** saves a relationship of type C between A and B */
  def save(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] =
    for {
      validate <- if (unique) validateUniqueness(c) else Future(true)
    } yield {
      if (validate) {
        val query =
          s"""
         match (a:${NeoNodeA.label}{ ${NeoNodeA.idColumn}: "${NeoNodeA.id(c.from)}"}),
         (b:${NeoNodeB.label}{ ${NeoNodeB.idColumn}: "${NeoNodeB.id(c.to)}"})
         create (a)-[c:${label} {props}]->(b)
         return c
      """.stripMargin
        val rows = Cypher(query).on("props" -> MapperC.caseToMap(c))().size
        rows > 0
      }
      else false
    }

  /** updates the relationship of type C */
  def update(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
         match (a:${NeoNodeA.label}{ ${NeoNodeA.idColumn}: "${NeoNodeA.id(c.from)}"}),
         (b:${NeoNodeB.label}{ ${NeoNodeB.idColumn}: "${NeoNodeB.id(c.to)}"}),
         (a)-[c:${label}]->(b)
         set c += {props}
         return c
      """.stripMargin
    val rows = Cypher(query).on("props" -> MapperC.caseToMap(c))().size
    rows > 0
  }

  /** deletes the relationship of type C */
  def delete(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
         match (a:${NeoNodeA.label}{ ${NeoNodeA.idColumn}: "${NeoNodeA.id(c.from)}"}),
         (b:${NeoNodeB.label}{ ${NeoNodeB.idColumn}: "${NeoNodeB.id(c.to)}"}),
         (a)-[c:${label}]->(b)
         delete c
      """.stripMargin
    Cypher(query).execute()
  }

}

/** Creates a NeoRel instance */
object NeoRel {
  def apply[C <: Rel[A, B] : Mapper, A: NeoNode, B: NeoNode](labelS: String, uniqueV: Boolean = false) = new NeoRel[C, A, B] {
    override val label: String = labelS
    override val unique: Boolean = uniqueV
  }
}

/** Converts C into an operations of C */
object NeoRelOperations {
  implicit def operations[C <: Rel[_, _]](c: C)(implicit neoRel: NeoRel[C, _, _]) = new NeoRelOperations(c, neoRel)
}

class NeoRelOperations[C <: Rel[_, _]](c: C, neoRel: NeoRel[C, _, _]) {

  /** Calls save on C */
  def save()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoRel.save(c)

  /** Calls update on C */
  def update()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoRel.update(c)

  /** Calls delete on C */
  def delete()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoRel.delete(c)

}