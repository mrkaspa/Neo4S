//package graph.model.orm
//
//import graph.test.{HelperTest, NeoTest}
//
//import scala.concurrent.Await
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.duration._
//
///**
// * Created by michelperez on 7/27/15.
// */
//class NodeSpec extends NeoTest with HelperTest{
//
//  describe("NeoRel"){
//
//    it("#save"){
//      withTwoNodes { (node1, node2) =>
//        val saved = Await.result(MyRelDAO.save(MyRel(node1, node2, true)), 2 seconds)
//        saved must be(true)
//      }
//    }
//
//  }
//
//}
