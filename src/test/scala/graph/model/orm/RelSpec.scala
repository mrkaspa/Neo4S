package graph.model.orm

import graph.test.{HelperTest, NeoTest}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import graph.model.orm.UserNodes._
import graph.model.orm.UserRels._

/**
 * Created by michelperez on 7/27/15.
 */
class RelSpec extends NeoTest with HelperTest{

  describe("NeoRel"){

    it("#save"){
      withTwoNodes { (node1, node2) =>
        val saved = Await.result(MyRel(node1, node2, true).save(), 2 seconds)
        saved must be(true)
      }
    }

  }

}
