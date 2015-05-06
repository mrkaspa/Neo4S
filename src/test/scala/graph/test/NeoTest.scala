package graph.test

import org.anormcypher.Neo4jREST
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSpec, MustMatchers}

/**
 * Created by michelperez on 4/26/15.
 */
trait NeoTest
  extends FunSpec
  with MustMatchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  implicit val connection = Neo4jREST("localhost", 7474, "/db/data/", "neo4j", "jokalive")

  override def beforeEach(): Unit = {
    NeoDBCleaner.cleanDB()
  }

}
