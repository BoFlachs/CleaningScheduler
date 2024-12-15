package cleaningscheduler.domain.search

import cleaningscheduler.domain.search.Tree.countNodes
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class TreeTests extends AnyWordSpec with Matchers {
  "countNodes" should {
    "count the nodes" in {
      val tree: Tree[Int] = Node(1, List(
        Node(2, List(Node(4, Nil), Node(5, Nil))),
        Node(3, List(Node(6, Nil), Node(7, Nil)))
      ))

      Tree.countNodes(tree) shouldBe 7
    }
  }

  "countLeafs" should {
    "count the leafs" in {
      val tree: Tree[Int] = Node(1, List(
        Node(2, List(Node(4, Nil), Node(5, Nil))),
        Node(3, List(Node(6, Nil), Node(7, Nil)))
      ))

      Tree.countLeafs(tree) shouldBe 4
    }
  }

  "collectLeafValues" should {
    "return a list of all values" in {
      val tree: Tree[Int] = Node(1, List(
        Node(2, List(Node(4, Nil), Node(5, Nil))),
        Node(3, List(Node(6, Nil), Node(7, Nil)))
      ))

      Tree.collectLeafValues(tree) shouldBe List(4, 5,  6, 7)
    }
  }

  "collectNodes" should {
    "return a list of all nodes" in {
      val tree: Tree[Int] = Node(1, List(
        Node(2, List(Node(4, Nil), Node(5, Nil))),
        Node(3, List(Node(6, Nil), Node(7, Nil)))
      ))

      val expectedNodesList = List(
        tree,
        Node(2, List(Node(4, Nil), Node(5, Nil))),
        Node(4, Nil), Node(5, Nil),
        Node(3, List(Node(6, Nil), Node(7, Nil))),
        Node(6, Nil), Node(7, Nil)
      )

      Tree.collectNodes(tree) shouldBe expectedNodesList
    }
  }
 }
