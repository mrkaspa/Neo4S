//package com.kreattiewe.neo4s.orm
//
///**
// * Created by michelperez on 4/27/15.
// *
// * every relationship has and start and end point
// */
//
//abstract class NeoRel[A: ({type L[x] = NeoNode[x, C]})#L, B: ({type L[x] = NeoNode[x, D]})#L, C, D] extends Labelable {
//  val neoFrom: NeoNode[A, C]
//  val neoTo: NeoNode[B, D]
//
//  def fromLabel: String = neoFrom.label
//
//  def fromId(a: A): Option[C] = neoFrom.id(a)
//
//  def toLabel: String = neoTo.label
//
//  def toId(b: B): Option[D] = neoTo.id(b)
//}
//
//object NeoRel {
//  def newRel[A: ({type L[x] = NeoNode[x, C]})#L, B: ({type L[x] = NeoNode[x, D]})#L, C, D](labelS: String) = new NeoRel[A, B, C, D] {
//    override val label: String = labelS
//    override val neoFrom: NeoNode[A, C] = implicitly[NeoNode[A, C]]
//    override val neoTo: NeoNode[B, D] = implicitly[NeoNode[B, D]]
//  }
//}
