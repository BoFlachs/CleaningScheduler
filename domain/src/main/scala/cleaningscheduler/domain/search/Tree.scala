package cleaningscheduler.domain.search

import scala.util.Random

sealed trait Tree[+A]

case class Node[+A](value: A, children: List[Tree[A]]) extends Tree[A]

object Tree:
  def countNodes[A](tree: Tree[A]): Int = tree match {
    case Node(_, children) => 1 + children.map(countNodes).sum
  }

  def countLeafs[A](tree: Tree[A]): Int = tree match {
    case Node(_, Nil) => 1
    case Node(_, children) => children.map(countLeafs).sum
  }

  def collectNodes[A](tree: Tree[A]): List[Node[A]] = tree match {
    case node@Node(_, children) =>
      node :: children.flatMap {
        child => collectNodes(child)
      }
  }

  def collectLeafValues[A](tree: Tree[A]): List[A] =
      tree match {
        case Node(value, Nil) => List(value)
        case node@Node(_, children) =>
          children.foldLeft(List.empty[A]) {
            (acc, child) => acc ++ collectLeafValues(child)
          }
      }