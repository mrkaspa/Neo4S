package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable

/**
 * Created by michelperez on 4/29/15.
 *
 * wraps the [[com.kreattiewe.mapper.macros.Mappable]] to convert case class to maps
 * and vice versa
 */
trait Mapper[T] {

  /** takes a map and returns the T instance*/
  def mapToCase[T: Mappable](map: Map[String, Any]) = {
    val newMap = map.map {
      case (k, v: BigDecimal) => (k, v.toInt)
      case (k, v) => (k, v)
    }
    implicitly[Mappable[T]].fromMap(newMap)
  }

  /** takes an instance of T and returns the Map*/
  def caseToMap[T: Mappable](t: T) = {
    val map = implicitly[Mappable[T]].toMap(t)
    t match {
      case _: NeoRel[_, _] => map -("to", "from")
      case _ => map
    }
  }

}