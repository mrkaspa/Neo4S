package graph.test

import graph.model.orm.{MyUserDAO, MyUser}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import graph.model.orm.UserNodes._

/**
 * Created by michelperez on 7/27/15.
 */
trait HelperTest {
  this: NeoTest =>

  def withOneNode(testCode: (MyUser, Boolean) => Any): Unit = {
    val node1 = MyUser("1", "Michel Perez", 27)
    val saved = Await.result(node1.save(), 5 seconds)
    testCode(node1, saved)
  }

  def withTwoNodes(testCode: (MyUser, MyUser) => Any): Unit = {
    val node1 = MyUser("1", "Michel Perez", 27)
    Await.result(MyUserDAO.save(node1), 2 seconds)
    val node2 = MyUser("2", "Michel Perez", 27)
    Await.result(MyUserDAO.save(node2), 2 seconds)
    testCode(node1, node2)
  }

}
