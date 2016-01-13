package com.mrkaspa.neo4s.orm

import org.anormcypher.CypherParser._
import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * Created by michelperez on 4/29/15.
 *
 * parses the results of a query
 */
object NeoQuery {

  /** returns the first node looked by the id of type A and labels */
  def findById[T: Mapper](id: String, label: Option[String] = None)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Option[T]] = {
    val MapperT = implicitly[Mapper[T]]
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

  /** parses a NeoNode to an instance of A */
  def transform[A](res: org.anormcypher.NeoNode, Mapper: Mapper[A]): Try[A] = {
    Try(Mapper.mapToCase(res.props))
  }

  /** parses a NeoNode to an instance of A */
  def transformI[A: Mapper](res: org.anormcypher.NeoNode)(implicit Mapper: Mapper[A]): Try[A] = {
    transform(res, Mapper)
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transform[A, B, C](res: (org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship), MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): Try[C] = {
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
  def transformI[A: Mapper, B: Mapper, C <: Rel[A, B]](res: (org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship))(implicit MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): Try[C] = {
    transform(res, MapperA, MapperB, MapperC)
  }

  /** parses the results from a list of NeoNodes to a list of A */
  def transform[A](res: List[org.anormcypher.NeoNode], Mapper: Mapper[A]): List[A] = {
    res.map(transform(_, Mapper)) collect { case Success(x) => x }
  }

  /** parses the results from a list of NeoNodes to a list of A */
  def transformI[A: Mapper](res: List[org.anormcypher.NeoNode])(implicit Mapper: Mapper[A]): List[A] = {
    transform(res, Mapper)
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transform[A, B, C](res: List[(org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship)], MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): List[C] = {
    res.map(transform(_, MapperA, MapperB, MapperC)) collect { case Success(x) => x }
  }

  /** parses the results from a list of (NeoNode, NeoNode, NeoRelationship) to a list of C[A,B] */
  def transformI[A: Mapper, B: Mapper, C <: Rel[A, B]](res: List[(org.anormcypher.NeoNode, org.anormcypher.NeoNode, org.anormcypher.NeoRelationship)])(implicit MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C]): List[C] = {
    transform(res, MapperA, MapperB, MapperC)
  }

  /** Execute a query that returns a List[A] */
  def executeQuery[A: Mapper](query: String)(implicit connection: Neo4jREST, Mapper: Mapper[A], ec: ExecutionContext): Future[List[A]] = Future {
    val res = Cypher(query).as(get[org.anormcypher.NeoNode]("a") *)
    transform(res, Mapper)
  }


  /** Execute a query that returns a List[A] */
  def executeQuery[A: Mapper, B: Mapper, C <: Rel[A, B]](query: String)(implicit connection: Neo4jREST, MapperA: Mapper[A], MapperB: Mapper[B], MapperC: Mapper[C], ec: ExecutionContext): Future[List[C]] = Future {
    val res = Cypher(query).as(get[org.anormcypher.NeoNode]("a") ~ get[org.anormcypher.NeoNode]("b") ~ get[org.anormcypher.NeoRelationship]("c") *).map(flatten)
    transform(res, MapperA, MapperB, MapperC)
  }

}
