package graph.model.orm

import com.kreattiewe.neo4s.orm.{NeoIndex, NeoQuery}
import graph.test.NeoTest
import org.anormcypher.Cypher
import org.anormcypher.CypherParser._
import scala.concurrent.duration._

import scala.concurrent.Await
import UserMappers._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

/**
 * Created by michelperez on 4/26/15.
 */
class NeoORMSpec extends NeoTest {

  def withIndex(testCode: (Boolean) => Any): Unit = {
    val indexed = Await.result(NeoIndex.create("user"), 2 seconds)
    testCode(indexed)
  }

  def withOneNode(testCode: (MyUser, Boolean) => Any): Unit = {
    val node1 = MyUser("1", "Michel Perez", 27)
    val saved = Await.result(MyUserDAO.save(node1), 2 seconds)
    testCode(node1, saved)
  }

  def withOneOptNode(testCode: (MyUserOpt, Boolean) => Any): Unit = {
    val node1 = MyUserOpt(Some("1"), "Michel Perez", None)
    val saved = Await.result(MyUserOptDAO.save(node1), 2 seconds)
    testCode(node1, saved)
  }

  def withTwoNodes(testCode: (MyUser, MyUser) => Any): Unit = {
    val node1 = MyUser("1", "Michel Perez", 27)
    Await.result(MyUserDAO.save(node1), 2 seconds)
    val node2 = MyUser("2", "Michel Perez", 27)
    Await.result(MyUserDAO.save(node2), 2 seconds)
    testCode(node1, node2)
  }

  def withOneRelation(testCode: (MyRel, Boolean) => Unit): Unit = {
    withTwoNodes { (node1, node2) =>
      val rel = MyRel(node1, node2, true)
      val saved = Await.result(MyRelDAO.save(rel), 2 seconds)
      testCode(rel, saved)
    }
  }

  describe("NeoDAO") {

    it("#save") {
      withOneNode { (_, saved) =>
        saved must be(true)
      }
    }

    describe("After save") {

      it("#findById") {
        withOneNode { (node, _) =>
          val user = Await.result(MyUserDAO.findById("1"), 5 seconds)
          user.isInstanceOf[Some[MyUser]] must be(true)
        }
      }

      it("Uses the NeoQuery") {
        withOneNode { (node, _) =>
          val query =
            s"""match (n:user { id: "1"}) return n""".stripMargin
          val res = Cypher(query).as(get[org.anormcypher.NeoNode]("n") *)
          val listRes = NeoQuery.transformI[MyUser](res)
          listRes.isInstanceOf[List[MyUser]] must be(true)
          listRes.size must be(1)
        }
      }

      it("#update") {
        withOneNode { (node, _) =>
          Await.result(MyUserDAO.update(node.copy(name = "Michel Perez Puentes")), 2 seconds)
          val user = Await.result(MyUserDAO.findById("1"), 5 seconds)
          user.get.name must be("Michel Perez Puentes")
        }
      }

      it("#delete") {
        withOneNode { (node, _) =>
          Await.result(MyUserDAO.delete(node), 2 seconds)
          val userQuery = Await.result(MyUserDAO.findById("1"), 5 seconds)
          userQuery must be(None)
        }
      }

    }

  }

  describe("NeoDAO with options") {

    it("#save") {
      withOneOptNode { (_, saved) =>
        saved must be(true)
      }
    }
    describe("After save") {

      it("#findById") {
        withOneOptNode { (node, _) =>
          val user = Await.result(MyUserOptDAO.findById(Some("1")), 5 seconds)
          user.isInstanceOf[Some[MyUserOpt]] must be(true)
          user.get.age == None must be(true)
        }
      }

      it("#findById unmarshalling error") {
        withOneOptNode { (node, _) =>
          val user = Await.ready(MyUserDAO.findById("1"), 5 seconds).value.get
          user.isFailure must be(true)
        }
      }

    }

  }

  describe("Relationships") {


    it("creates a relation") {
      withTwoNodes { (node1, node2) =>
        val saved = Await.result(MyRelDAO.save(MyRel(node1, node2, true)), 2 seconds)
        saved must be(true)
      }
    }

    describe("After the relation is created") {

      it("error saving the second relation") {
        withOneRelation { (rel, _) =>
          val saved = Await.result(MyRelDAO.save(rel.copy(enabled = false)), 2 seconds)
          saved must be(false)
        }
      }

      it("updates enabled to false") {
        withOneRelation { (rel, _) =>
          val updated = Await.result(MyRelDAO.update(rel.copy(enabled = false)), 2 seconds)
          updated must be(true)
        }
      }

      it("deletes the rel") {
        withOneRelation { (rel, _) =>
          val deleted = Await.result(MyRelDAO.delete(rel), 2 seconds)
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

  describe("Update and add params") {

    it("creates a MyUser and after updates a MyUserExp") {
      val user = MyUser("1", "Michel Perez", 27)
      val userExp = MyUserExp("1", "Michel Perez", "demo@demo.com")
      Await.result(MyUserDAO.save(user), 2 seconds)
      val saved = Await.result(MyUserExpDAO.update(userExp), 2 seconds)
      saved must be(true)
      val userFound = Await.result(MyUserDAO.findById("1"), 5 seconds)
      userFound.isInstanceOf[Some[MyUser]] must be(true)
      val userFoundExp = Await.result(MyUserExpDAO.findById("1"), 5 seconds)
      userFoundExp.isInstanceOf[Some[MyUserExp]] must be(true)
    }

  }

  describe("withIndex") {

    it("creates the index") {
      withIndex { indexed =>
        indexed must be(true)
      }
    }

    it("tests the index") {
      withIndex { indexed =>
        val saved = Await.result(MyUserDAO.save(MyUser("1", "Michel Perez", 27)), 2 seconds)
        saved must be(true)
        val savedAgain = Await.result(MyUserDAO.save(MyUser("1", "Michel Perez", 27)), 2 seconds)
        savedAgain must be(false)
      }
    }

  }

}
