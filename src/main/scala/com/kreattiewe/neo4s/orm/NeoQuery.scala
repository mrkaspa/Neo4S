package com.kreattiewe.neo4s.orm

import org.anormcypher.CypherParser._
import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

/**
 * Created by michelperez on 4/29/15.
 *
 * parses the results of a query
 */
object NeoQuery {

  /** parses a NeoNode to an instance of A */
  def transform[A <: NeoNode[_]](res: org.anormcypher.NeoNode, Mapper: Mapper[A]): Try[A] = {
    Try(Mapper.mapToCase(res.props))
  }

  /** parses a NeoNode to an instance of A */
  def transformI[A <: NeoNode[_]](res: org.anormcypher.NeoNode)(implicit Mapper: Mapper[A]): Try[A] = {
    transform(res, Mapper)
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transform[A <: NeoNode[_], B <: NeoNode[_], C <: NeoRel[A, B]](res: (org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship), MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): Try[C] = {
    Try {
      res match {
        case (a, b, c) =>
          val from = MapperA.mapToCase(a.props)
          val to = MapperB.mapToCase(b.props)
          val props = c.props +("to" -> to, "from" -> from)
          MapperC.mapToCase(props)
      }
    }
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transformI[A <: NeoNode[_], B <: NeoNode[_], C <: NeoRel[A, B]](res: (org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship))(implicit MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): Try[C] = {
    transform(res, MapperA, MapperB, MapperC)
  }

  /** parses the results from a list of NeoNodes to a list of A */
  def transform[A <: NeoNode[_]](res: List[org.anormcypher.NeoNode], Mapper: Mapper[A]): List[A] = {
    res.map(transform(_, Mapper)) collect { case Success(x) => x }
  }

  /** parses the results from a list of NeoNodes to a list of A */
  def transformI[A <: NeoNode[_] : Mapper](res: List[org.anormcypher.NeoNode])(implicit Mapper: Mapper[A]): List[A] = {
    transform(res, Mapper)
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transform[A <: NeoNode[_], B <: NeoNode[_], C <: NeoRel[A, B]](res: List[(org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship)], MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): List[C] = {
    res.map(transform(_, MapperA, MapperB, MapperC)) collect { case Success(x) => x }
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transformI[A <: NeoNode[_] : Mapper, B <: NeoNode[_] : Mapper, C <: NeoRel[A, B] : Mapper](res: List[(org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship)])(implicit MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): List[C] = {
    transform(res, MapperA, MapperB, MapperC)
  }

  /** Execute a query that returns a List[A] */
  def executeQuery[A <: NeoNode[_] : Mapper](query: String)(implicit connection: Neo4jREST, Mapper: Mapper[A], ec: ExecutionContext): Future[List[A]] = Future {
    val res = Cypher(query).as(get[org.anormcypher.NeoNode]("a") *)
    transform(res, Mapper)
  }


  /** Execute a query that returns a List[A] */
  def executeQuery[A <: NeoNode[_] : Mapper, B <: NeoNode[_] : Mapper, C <: NeoRel[A, B] : Mapper](query: String)(implicit connection: Neo4jREST, MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C], ec: ExecutionContext): Future[List[C]] = Future {
    val res = Cypher(query).as(get[org.anormcypher.NeoNode]("a") ~ get[org.anormcypher.NeoNode]("b") ~ get[org.anormcypher.NeoRelationship]("c") *).map(flatten)
    transform(res, MapperA, MapperB, MapperC)
  }

}
