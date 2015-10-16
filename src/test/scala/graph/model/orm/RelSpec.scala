package graph.model.orm

import com.kreattiewe.neo4s.orm.{NeoRelOperations, NeoQuery}
import graph.test.{HelperTest, NeoTest}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import com.kreattiewe.neo4s.orm.NeoNodeOperations._
import com.kreattiewe.neo4s.orm.NeoRelOperations._
import graph.model.orm.UserNodes._
import graph.model.orm.UserRels._

import graph.model.orm.UserMappers._

import org.anormcypher.Cypher
import org.anormcypher.CypherParser._

/**
 * Created by michelperez on 7/27/15.
 */
class RelSpec extends NeoTest with HelperTest {

  describe("NeoRel") {

    it("creates a relation") {
      withTwoNodes { (node1, node2) =>
        val op = new NeoRelOperations(MyRel(node1, node2, true), userRel)
        val rel : MyRel = MyRel(node1, node2, true)
        rel.save()

        val saved = Await.result(op.save(), 2 seconds)
        saved must be(true)
      }
    }

    describe("After the relation is created") {

      it("creates a rel with Seq and after updates the enabled") {
        withTwoNodes { (node1, node2) =>
          val myRelSeq = MyRelSeq(node1, node2, true, Seq(1.23, 23.4))
          val saved = Await.result(myRelSeq.save(), 2 seconds)
          saved must be(true)
          val query =
            s"""
                  match (a:user { id: "${node1.id}"}), (b:user { id: "${node2.id}"}),
                  (a)-[c:friendship]->(b) return a, b, c
                """.stripMargin
          val fut = NeoQuery.executeQuery[MyUser, MyUser, MyRelSeq](query) map {
            case List(res) => Some(res)
            case _ => None
          }
          val friendship = Await.result(fut, 2 seconds)
          val updated = Await.result(friendship.get.copy(enabled = true).update(), 2 seconds)
          updated must be(true)
        }
      }

      it("error saving the second relation") {
        withOneRelation { (rel, _) =>
          val saved = Await.result(rel.copy(enabled = false).save(), 2 seconds)
          saved must be(false)
        }
      }

      it("updates enabled to false") {
        withOneRelation { (rel, _) =>
          val updated = Await.result(rel.copy(enabled = false).update(), 2 seconds)
          updated must be(true)
        }
      }

      it("deletes the rel") {
        withOneRelation { (rel, _) =>
          val deleted = Await.result(rel.delete(), 2 seconds)
          deleted must be(true)
        }
      }

      it("deletes one node with rel fails") {
        withOneRelation { (rel, _) =>
          val deleted = Await.result(rel.to.delete(), 2 seconds)
          deleted must be(false)
        }
      }

      it("deletes one node with rel works") {
        withOneRelation { (rel, _) =>
          val deleted = Await.result(rel.to.deleteWithRelations(), 2 seconds)
          deleted must be(true)
        }
      }

      it("queries the rel") {
        withOneRelation { (rel, _) =>
          val query =
            s"""match (a:user)-[c:friendship]->(b:user)
                      return a, b, c """.stripMargin
          val res = Cypher(query).as(get[org.anormcypher.NeoNode]("a") ~ get[org.anormcypher.NeoNode]("b") ~ get[org.anormcypher.NeoRelationship]("c") *).map(flatten)
          val listRes = NeoQuery.transformI[MyUser, MyUser, MyRel](res)
          listRes.size must be(1)
          listRes(0) must be(rel)
        }
      }

      it("queries the rel using executeQuery") {
        withOneRelation { (rel, _) =>
          val query =
            s"""match (a:user)-[c:friendship]->(b:user)
                      return a, b, c """.stripMargin
          val listRes = Await.result(NeoQuery.executeQuery[MyUser, MyUser, MyRel](query), 2 seconds)
          listRes.size must be(1)
          listRes(0) must be(rel)
        }
      }

    }

  }

}
