package com.kreattiewe.neo4s.orm

/**
 * Created by michelperez on 4/26/15.
 *
 * every node must be recognizable through an id
 */
trait NeoNode[T] extends Labelable{
  val id: T

  /** Unwraps the id if this comes in Option */
  def getId() = {
    id match {
      case opt@Some(_id) => _id
      case _ => id
    }
  }
}
