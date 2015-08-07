package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable
import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{Future, ExecutionContext}
import scala.reflect.runtime.universe._

/**
 * Created by michelperez on 4/27/15.
 *
 * every relationship has and start and end point
 */

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
         match (a:${NeoNodeA.label}{ id: "${NeoNodeA.id(c.from)}"})-[c:${label}]->(b:${NeoNodeB.label}{ id: "${NeoNodeB.id(c.to)}"})
         return c
       """.stripMargin
    Cypher(query)().size == 0
  }


  /** saves a relationship of type C between A and B */
  def save(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] =
    for {
      validate <- if (unique) validateUniqueness(c) else Future(true)
    } yield {
      if (validate) {
        val query =
          s"""
         match (a:${NeoNodeA.label}{ id: "${NeoNodeA.id(c.from)}"}),
         (b:${NeoNodeB.label}{ id: "${NeoNodeB.id(c.to)}"})
         create (a)-[c:${label} {props}]->(b)
      """.stripMargin
        Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
      }
      else false
    }

  /** updates the relationship of type C */
  def update(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
         match (a:${NeoNodeA.label}{ id: "${NeoNodeA.id(c.from)}"}),
         (b:${NeoNodeB.label}{ id: "${NeoNodeB.id(c.to)}"})
         (a)-[c:${label}]->(b)
         set c += {props}
      """.stripMargin
    Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
  }

  /** deletes the relationship of type C */
  def delete(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
    val query =
      s"""
         match (a:${NeoNodeA.label}{ id: "${NeoNodeA.id(c.from)}"}),
         (b:${NeoNodeB.label}{ id: "${NeoNodeB.id(c.to)}"})
         (a)-[c:${label}]->(b)
         delete c
      """.stripMargin
    Cypher(query).execute()
  }

  def operations(c: C) = new NeoRelOperations(c)

  class NeoRelOperations(c: C) {

    def save()(implicit connection: Neo4jREST, ec: ExecutionContext) = NeoRel.this.save(c)

    def update()(implicit connection: Neo4jREST, ec: ExecutionContext) = NeoRel.this.update(c)

    def delete()(implicit connection: Neo4jREST, ec: ExecutionContext) = NeoRel.this.delete(c)

  }

}

object NeoRel {
  def apply[C <: Rel[A, B] : Mapper, A: NeoNode, B: NeoNode](labelS: String, uniqueV: Boolean = false) = new NeoRel[C, A, B] {
    override val label: String = labelS
    override val unique: Boolean = uniqueV
  }
}