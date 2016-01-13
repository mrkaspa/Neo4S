package com.mrkaspa.neo4s.orm

/**
 * Created by michelperez on 4/29/15.
 *
 * every node and relationship must havea a set of labels that are mapped to the
 * neo4j labels
 */
trait Labelable {

  /** set of labels for the node or rel*/
  val label: String

}
