package graph.model.orm

import com.kreattiewe.neo4s.orm.NeoQuery
import graph.test.{HelperTest, NeoTest}
import org.anormcypher.Cypher
import org.anormcypher.CypherParser._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import com.kreattiewe.neo4s.orm.NeoNodeOperations._
import com.kreattiewe.neo4s.orm.NeoRelOperations._
import graph.model.orm.UserNodes._
import graph.model.orm.UserRels._
import graph.model.orm.UserMappers._

/**
 * Created by michelperez on 7/27/15.
 */
class NodeSpec extends NeoTest with HelperTest {

  describe("NeoDAO") {

    it("#save") {
      withOneNode { (_, saved) =>
        saved must be(true)
      }
    }

    describe("After save") {

      it("#findById") {
        withOneNode { (node, _) =>
          val user = Await.result(NeoQuery.findById[MyUser]("1", Some("user")), 5 seconds)
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
          Await.result(node.copy(name = "Michel Perez Puentes").update(), 2 seconds)
          val user = Await.result(NeoQuery.findById[MyUser]("1", Some("user")), 5 seconds)
          user.get.name must be("Michel Perez Puentes")
        }
      }

      it("#delete") {
        withOneNode { (node, _) =>
          Await.result(node.delete(), 2 seconds)
          val userQuery = Await.result(NeoQuery.findById[MyUser]("1", Some("user")), 5 seconds)
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
          val user = Await.result(NeoQuery.findById[MyUserOpt]("1", Some("user")), 5 seconds)
          user.isInstanceOf[Some[MyUserOpt]] must be(true)
          user.get.age == None must be(true)
        }
      }

      it("#findById unmarshalling error") {
        withOneOptNode { (node, _) =>
          val user = Await.ready(NeoQuery.findById[MyUser]("1", Some("user")), 5 seconds).value.get
          user.isFailure must be(true)
        }
      }

    }

  }

  describe("Update and add params") {

    it("creates a MyUser and after updates a MyUserExp") {
      val user = MyUser("1", "Michel Perez", 27)
      val userExp = MyUserExp("1", "Michel Perez", "demo@demo.com")
      Await.result(user.save(), 2 seconds)
      val saved = Await.result(userExp.update(), 2 seconds)
      saved must be(true)
      val userFound = Await.result(NeoQuery.findById[MyUser]("1", Some("user")), 5 seconds)
      userFound.isInstanceOf[Some[MyUser]] must be(true)
      val userFoundExp = Await.result(NeoQuery.findById[MyUserExp]("1", Some("user")), 5 seconds)
      userFoundExp.isInstanceOf[Some[MyUserExp]] must be(true)
    }

  }

}
