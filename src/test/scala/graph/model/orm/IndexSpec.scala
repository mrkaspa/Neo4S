package graph.model.orm

import graph.test.{HelperTest, NeoTest}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import graph.model.orm.UserNodes._

/**
 * Created by michelperez on 8/7/15.
 */
class IndexSpec extends NeoTest with HelperTest {

  describe("NeoIndex") {

    it("creates the index") {
      withIndex { indexed =>
        indexed must be(true)
      }
    }

    it("tests the index") {
      withIndex { indexed =>
        val saved = Await.result(MyUser("1", "Michel Perez", 27).save(), 2 seconds)
        saved must be(true)
        val savedAgain = Await.result(MyUser("1", "Michel Perez", 27).save(), 2 seconds)
        savedAgain must be(false)
      }
    }

  }

}
