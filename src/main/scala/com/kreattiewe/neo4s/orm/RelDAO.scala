package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable
import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by michelperez on 4/29/15.
 *
 * creates a relationship dao with the following feature
 * A = Node Source
 * B = Node Destitny
 * C = Rel
 **/
trait RelDAO[A <: NeoNode[_], B <: NeoNode[_], C <: NeoRel[A, B]] {

  object MapperA extends Mapper[A]

  object MapperB extends Mapper[B]

  object MapperC extends Mapper[C]

  /** saves a relationship of type C between A and B */
  def save[C <: NeoRel[A, B] : Mappable](c: C)(implicit connection: Neo4jREST): Future[Boolean] = {
    val query =
      s"""
         match (a${c.from.labelsString()}{ id: "${c.from.id}"}),
         (b${c.to.labelsString()}{ id: "${c.to.id}"})
         create (a)-[c${c.labelsString()} {props}]->(b)
      """.stripMargin
    Future {
      Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
    }
  }

  /** updates the relationship of type C */
  def update[C <: NeoRel[A, B] : Mappable](c: C)(implicit connection: Neo4jREST): Future[Boolean] = {
    val query =
      s"""
         match (a${c.from.labelsString()}{ id: "${c.from.id}"}),
         (b${c.to.labelsString()}{ id: "${c.to.id}"}),
         (a)-[c${c.labelsString()}]->(b)
         set c = {props}
      """.stripMargin
    Future {
      Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
    }
  }

  /** deletes the relationship of type C */
  def delete[C <: NeoRel[A, B] : Mappable](c: C)(implicit connection: Neo4jREST): Future[Boolean] = {
    val query =
      s"""
         match (a ${c.from.labelsString()}{ id: "${c.from.id}"}),
         (b ${c.to.labelsString()}{ id: "${c.to.id}"}),
         (a)-[c${c.labelsString()}]->(b)
         delete c
      """.stripMargin
    Future {
      Cypher(query).execute()
    }
  }

}
