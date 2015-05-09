package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable

import scala.reflect.runtime.universe._


/**
 * Created by michelperez on 4/29/15.
 *
 * wraps the [[com.kreattiewe.mapper.macros.Mappable]] to convert case class to maps
 * and vice versa
 */
abstract class Mapper[T: Mappable : TypeTag] {

  /** takes a map and returns the T instance */
  def mapToCase(map: Map[String, Any]) = {
    val tpe = typeTag[T].tpe
    val optType = typeOf[Option[_]]
    val optFields = tpe.decls.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get.paramLists.head
      .filter(_.asTerm.info <:< optType).map(_.asTerm.name.decodedName.toString)
    val mapWithNones = optFields.foldLeft(map) {
      (newMap, fieldName) => if (!newMap.contains(fieldName)) newMap + (fieldName -> None) else newMap
    }
    val newMap = mapWithNones.map {
      case (k, v) =>
        if (optFields.exists(_ == k)) {
          val newV = v match {
            case None => None
            case va: BigDecimal => Some(va.toInt)
            case va => Some(va)
          }
          (k, newV)
        } else {
          val newV = v match {
            case va: BigDecimal => va.toInt
            case va => va
          }
          (k, newV)
        }
    }
    implicitly[Mappable[T]].fromMap(newMap)
  }

  /** takes an instance of T and returns the Map */
  def caseToMap(t: T) = {
    val map = implicitly[Mappable[T]].toMap(t)
    val mapFiltered = if (t.isInstanceOf[NeoRel[_, _]]) map -("to", "from") else map
    mapFiltered.filter({
      case (k, None) => false
      case _ => true
    }).map({
      case (k, v@Some(a)) => (k, a)
      case (k, v) => (k, v)
    })
  }

}

object Mapper {

  def build[T: Mappable : TypeTag]: Mapper[T] = new Mapper[T] {}

}