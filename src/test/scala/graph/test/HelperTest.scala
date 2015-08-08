package graph.test

import com.kreattiewe.neo4s.orm.NeoIndex
import graph.model.orm._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import graph.model.orm.UserNodes._
import graph.model.orm.UserRels._

/**
 * Created by michelperez on 7/27/15.
 */
trait HelperTest {
  this: NeoTest =>

  def withIndex(testCode: (Boolean) => Any): Unit = {
    val indexed = Await.result(NeoIndex.create("user"), 2 seconds)
    testCode(indexed)
  }

  def withOneNode(testCode: (MyUser, Boolean) => Any): Unit = {
    val node1 = MyUser("1", "Michel Perez", 27)
    val saved = Await.result(node1.save(), 2 seconds)
    testCode(node1, saved)
  }

  def withOneOptNode(testCode: (MyUserOpt, Boolean) => Any): Unit = {
    val node1 = MyUserOpt(Some("1"), "Michel Perez", None)
    val saved = Await.result(node1.save(), 2 seconds)
    testCode(node1, saved)
  }

  def withTwoNodes(testCode: (MyUser, MyUser) => Any): Unit = {
    val node1 = MyUser("1", "Michel Perez", 27)
    Await.result(node1.save(), 2 seconds)
    val node2 = MyUser("2", "Michel Perez", 27)
    Await.result(node2.save(), 2 seconds)
    testCode(node1, node2)
  }

  def withOneRelation(testCode: (MyRel, Boolean) => Unit): Unit = {
    withTwoNodes { (node1, node2) =>
      val rel = MyRel(node1, node2, true)
      val saved = Await.result(rel.save(), 2 seconds)
      testCode(rel, saved)
    }
  }

}
