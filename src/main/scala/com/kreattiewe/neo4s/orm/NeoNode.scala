package com.kreattiewe.neo4s.orm

import org.anormcypher.{Cypher, Neo4jREST}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by michelperez on 4/26/15.
 *
 * every node must be recognizable through an id
 */
abstract class NeoNode[T: Mapper, A] extends Labelable {

  val Mapper = implicitly[Mapper[T]]

  /** Unwraps the id if this comes in Option */
  def id(t: T): Option[A]

  def save(t: T)(implicit connection: Neo4jREST, ec: ExecutionContext): Future[Boolean] = Future {

    if (id(t).isEmpty) false
    else {
      val query =
        s"create (n:${label} {props})".stripMargin

      Cypher(query).on("props" -> Mapper.caseToMap(t)).execute()
    }
  }

}

object NeoNode {

  def apply[T: Mapper, A](labelS: String, f: T => Option[A]) = new NeoNode[T, A] {
    override def id(t: T): Option[A] = f(t)

    override val label: String = labelS
  }

}

class NeoNodeOperations[T: ({type L[x] = NeoNode[x, A]})#L : Mapper, A](t: T) {

  val neoNode = implicitly[NeoNode[T, A]]

  def save()(implicit connection: Neo4jREST, ec: ExecutionContext) = neoNode.save(t)

}

object NeoNodeOperations {
  def apply[T: ({type L[x] = NeoNode[x, A]})#L : Mapper, A](t: T) = new NeoNodeOperations(t)
}