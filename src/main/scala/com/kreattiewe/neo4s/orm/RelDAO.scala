package com.kreattiewe.neo4s.orm

import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by michelperez on 4/29/15.
 *
 * creates a relationship dao with the following feature
 * A = Node Source
 * B = Node Destitny
 * C = Rel
 **/
abstract class RelDAO[A <: NeoNode[_] : Mapper, B <: NeoNode[_] : Mapper, C <: NeoRel[A, B] : Mapper]
(implicit val MapperA: Mapper[A], implicit val MapperB: Mapper[B], implicit val MapperC: Mapper[C]) {

  /** saves a relationship of type C between A and B */
  def save(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = {
    val query =
      s"""
         match (a${c.from.labelsString()}{ id: "${c.from.getId()}"}),
         (b${c.to.labelsString()}{ id: "${c.to.getId()}"})
         create (a)-[c${c.labelsString()} {props}]->(b)
      """.stripMargin
    Future {
      Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
    }
  }

  /** updates the relationship of type C */
  def update(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = {
    val query =
      s"""
         match (a${c.from.labelsString()}{ id: "${c.from.getId()}"}),
         (b${c.to.labelsString()}{ id: "${c.to.getId()}"}),
         (a)-[c${c.labelsString()}]->(b)
         set c += {props}
      """.stripMargin
    Future {
      Cypher(query).on("props" -> MapperC.caseToMap(c)).execute()
    }
  }

  /** deletes the relationship of type C */
  def delete(c: C)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = {
    val query =
      s"""
         match (a ${c.from.labelsString()}{ id: "${c.from.getId()}"}),
         (b ${c.to.labelsString()}{ id: "${c.to.getId()}"}),
         (a)-[c${c.labelsString()}]->(b)
         delete c
      """.stripMargin
    Future {
      Cypher(query).execute()
    }
  }

}
