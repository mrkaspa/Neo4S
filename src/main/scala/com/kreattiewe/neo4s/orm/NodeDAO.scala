//package com.kreattiewe.neo4s.orm
//
//import org.anormcypher.CypherParser._
//import org.anormcypher.{Cypher, Neo4jREST}
//
//import scala.concurrent.{Promise, ExecutionContext, Future}
//import scala.util.{Success, Failure, Try}
//
///**
// * Created by michelperez on 4/25/15.
// *
// * extend it to create a dao of type T
// */
//abstract class NodeDAO[T :NeoNode : Mapper] {
//
//  val Mapper = implicitly[Mapper[T]]
//  val neoNode = implicitly[NeoNode[T]]
//
//  /** saves a node of type T */
//  def save(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
//    if (neoNode.id(t).isEmpty) false
//    else {
//      val query =
//        s"create (n:${neoNode.label} {props})".stripMargin
//
//      Cypher(query).on("props" -> Mapper.caseToMap(t)).execute()
//    }
//  }
//
//  /** updates a node of type T looking for t.id */
//  def update(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
//    val query =
//      s"""
//        match (n:${neoNode.label} { id: "${neoNode.id(t)}"})
//        set n += {props}
//        """.stripMargin
//    Cypher(query).on("props" -> Mapper.caseToMap(t)).execute()
//  }
//
//  /** deletes a node of type T looking for t.id */
//  def delete(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
//    val query =
//      s"""match (n:${neoNode.label} { id: "${neoNode.id(t)}"}) delete n""".stripMargin
//    Cypher(query).execute()
//  }
//
//  /** deletes a node of type T looking for t.id with its incomming and outgoing relations */
//  def deleteWithRelations(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {
//    val query =
//      s"""match (n:${neoNode.label} { id: "${neoNode.id(t)}"})-[r]-() delete r, n""".stripMargin
//    Cypher(query).execute()
//  }
//
//  /** returns the first node looked by the id of type A and labels */
//  def findById(id: String, label: Option[String] = None)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Option[T]] = {
//    val promisedOption = Promise[Option[T]]()
//    Future {
//      val labelStr = if (label.isEmpty) "" else s":${label.get}"
//      val query =
//        s"""match (n$labelStr { id: "$id"}) return n""".stripMargin
//      val resOptTry = Cypher(query).as(get[org.anormcypher.NeoNode]("n") *).collectFirst({ case n => n }).map({ case n => NeoQuery.transform(n, Mapper) })
//      resOptTry match {
//        case Some(resTry) =>
//          resTry match {
//            case Success(res) => promisedOption.success(Some(res))
//            case Failure(e) => promisedOption.failure(e)
//          }
//        case None => promisedOption.success(None)
//      }
//    }
//    promisedOption.future
//  }
//
//}
