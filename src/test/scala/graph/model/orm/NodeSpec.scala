package graph.model.orm

import graph.test.{HelperTest, NeoTest}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created by michelperez on 7/27/15.
 */
class NodeSpec extends NeoTest with HelperTest{

  describe("NeoDAO") {

    it("#save") {
      withOneNode { (_, saved) =>
        saved must be(true)
      }
    }

  }
}
