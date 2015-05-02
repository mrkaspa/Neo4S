package com.kreattiewe.neo4s.orm

/**
 * Created by michelperez on 4/27/15.
 *
 * every relationship has and start and end point
 */
trait NeoRel[A <: NeoNode[_], B <: NeoNode[_]] extends Labelable{
  val from: A
  val to: B
}
