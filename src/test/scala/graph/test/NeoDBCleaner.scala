package graph.test

import org.anormcypher.{Cypher, Neo4jREST}

/**
 * Created by michelperez on 4/26/15.
 */
object NeoDBCleaner {

  def cleanDB()(implicit connection: Neo4jREST): Boolean = {
    Cypher("match (m)-[r]->(n) delete r").execute()
    Cypher("match (n) delete n").execute()
  }

}
