package com.mrkaspa.neo4s.orm

import com.kreattiewe.mapper.macros.Mappable

import scala.collection.mutable.ListBuffer
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
    val params = tpe.decls.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get.paramLists.head
    val extractName = (r: Symbol) => r.asTerm.name.decodedName.toString
    val optFields = params.filter(_.asTerm.info <:< typeOf[Option[_]]).map(extractName)
    val intFields = params.filter((x) => x.asTerm.info <:< typeOf[Int] || x.asTerm.info <:< typeOf[Seq[Int]]).map(extractName)
    val doubleFields = params.filter((x) => x.asTerm.info <:< typeOf[Double] || x.asTerm.info <:< typeOf[Seq[Double]]).map(extractName)
    val mapWithNones = optFields.foldLeft(map) {
      (newMap, fieldName) => if (!newMap.contains(fieldName)) newMap + (fieldName -> None) else newMap
    }
    def transformBigDecimal(fieldName: String, va: BigDecimal): Any = {
      if (intFields.contains(fieldName)) va.toInt
      else if (doubleFields.contains(fieldName)) va.toDouble
    }
    val newMap = mapWithNones.map {
      case (k, v) =>
        if (optFields.exists(_ == k)) {
          val newV = v match {
            case None => None
            case va: BigDecimal => Some(transformBigDecimal(k, va))
            case va: ListBuffer[BigDecimal] => Some(va.toSeq.map(transformBigDecimal(k, _)))
            case va: ListBuffer[_] => Some(va.toSeq)
            case va => Some(va)
          }
          (k, newV)
        } else {
          val newV = v match {
            case va: BigDecimal => transformBigDecimal(k, va)
            case va: ListBuffer[BigDecimal] => va.toSeq.map(transformBigDecimal(k, _))
            case va: ListBuffer[_] => va.toSeq
            case va => va
          }
          (k, newV)
        }
    }
    implicitly[Mappable[T]].fromMap(newMap)
  }

  /** takes an instance of T and returns the Map */
  def caseToMap(t: T) = {
    //    implicitly[Mappable[T]].toMap(t)
    val map = implicitly[Mappable[T]].toMap(t)

    val mapFiltered = if (t.isInstanceOf[Rel[_, _]]) map -("to", "from") else map
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