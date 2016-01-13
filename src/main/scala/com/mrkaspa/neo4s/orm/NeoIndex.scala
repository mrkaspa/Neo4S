package com.mrkaspa.neo4s.orm

import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by michelperez on 5/6/15.
 */
object NeoIndex {

  def create(label: String)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = {
    create(label, "id")
  }

  def create(label: String, column: String)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = {
    val query = s"""create constraint on (n:$label) assert n.$column is unique"""
    Future {
      Cypher(query).execute()
    }
  }

  def drop(label: String)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = {
    drop(label, "id")
  }

  def drop(label: String, column: String)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = {
    val query = s"""drop constraint on (n:$label) assert n.$column is unique"""
    Future {
      Cypher(query).execute()
    }
  }

}
