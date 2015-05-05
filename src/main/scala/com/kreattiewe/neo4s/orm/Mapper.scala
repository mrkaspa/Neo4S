package com.kreattiewe.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable

import scala.reflect.runtime.universe._


/**
 * Created by michelperez on 4/29/15.
 *
 * wraps the [[com.kreattiewe.mapper.macros.Mappable]] to convert case class to maps
 * and vice versa
 */
trait Mapper[T] {

  /** takes a map and returns the T instance */
  def mapToCase[T: Mappable : TypeTag](map: Map[String, Any]) = {
    val tpe = typeTag[T].tpe
    val optType = typeOf[Option[_]]
    val optFields = tpe.decls.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get.paramLists.head.filter { field =>
      field.asTerm.info <:< optType
    }

    val newMap = map.map {
      case (k, v) =>
        if (optFields.exists { field => field.asTerm.name.decodedName.toString == k }) {
          val newV = v match {
            case null => None
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
  def caseToMap[T: Mappable](t: T) = {
    val map = implicitly[Mappable[T]].toMap(t)
    val mapFiltered = t match {
      case _: NeoRel[_, _] => map -("to", "from")
      case _ => map
    }
    val mapWithoutOptionals = mapFiltered.map {
      case (k, v@Some(a)) => (k, a)
      case (k, None) => (k, null)
      case (k, v) => (k, v)
    }
    mapWithoutOptionals
  }

}