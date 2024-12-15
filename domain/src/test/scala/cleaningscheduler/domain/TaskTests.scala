package cleaningscheduler.domain

import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps
import scala.util.{Failure, Success}

class TaskTests extends AnyWordSpec {
  "Task" when {
    "constructed" should {
      "have name" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        assert(task.name == "task1")
      }

      "throw error if minRepeatInterval is smaller than maxRepeatInterval" in {
        val person = Person("PersonA", Map[Int, Int]())
        assertThrows[IllegalArgumentException](
          Task("task1", 30, person, true, 30, true, 5, 1)
        )
      }
      "throw error if costs are zero or smaller" in {
        val person = Person("PersonA", Map[Int, Int]())
        assertThrows[IllegalArgumentException](
          Task("task1", -10, person, true, 30, true, 1, 5)
        )
      }
      "throw error if lastDoneAt is not a week number" in {
        val person = Person("PersonA", Map[Int, Int]())
        assertThrows[IllegalArgumentException](
          Task("task1", 10, person, true, 0, true, 1, 5)
        )
        assertThrows[IllegalArgumentException](
          Task("task1", 10, person, true, 60, true, 1, 5)
        )
      }
    }
    "changed" should {
      "create new task with new name" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        val updatedTask = Task.changeName(task, "task2")
        assert(task !== updatedTask)
        assert(updatedTask.name == "task2")
      }
      "create new task with new costs" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        val updatedTask = Task.changeCosts(task, 40)
        assert(task !== updatedTask)
        assert(updatedTask.costs == 40)
      }
      "create new task with new preferred assignee" in {
        val person = Person("PersonA", Map[Int, Int]())
        val newPerson = Person("PersonB", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        val updatedTask = Task.changePreferredAssignee(task, newPerson)
        assert(task !== updatedTask)
        assert(updatedTask.preferredAssignee == newPerson)
      }
      "create new task with new isPreferredFixed" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        val updatedTask = Task.changeIsPreferredFixed(task, false)
        assert(task !== updatedTask)
        assert(!updatedTask.isPreferredFixed)
      }
      "create new task with new last done week" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        val updatedTask = Task.changeLastDoneAt(task, 32)
        assert(task !== updatedTask)
        assert(updatedTask.lastDoneAt == 32)
      }
      "create new task with new isRepeated" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        val updatedTask = Task.changeIsRepeated(task, false)
        assert(task !== updatedTask)
        assert(!updatedTask.isRepeated)
      }
      "create new task with new min/maxRepeatInterval" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)
        val updatedMinTaskTry = Task.changeMinRepeatInterval(task, 1)

        updatedMinTaskTry match
          case Success(t) =>
            assert(task !== updatedMinTaskTry)
            assert(t.minRepeatInterval == 1)
          case Failure(e) => assert(false)

        val updatedMaxTaskTry = Task.changeMaxRepeatInterval(task, 5)

        updatedMaxTaskTry match
          case Success(t) =>
            assert(task !== updatedMaxTaskTry)
            assert(t.maxRepeatInterval == 5)
          case Failure(e) => assert(false)
      }
      "throw error if min-/maxRepeatInterval is larger than max" in {
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, true, 30, true, 2, 3)

        val updatedMinTaskTry = Task.changeMinRepeatInterval(task, 5)
        updatedMinTaskTry match
          case Success(t) => assert(false)
          case Failure(e) => assert(e.isInstanceOf[IllegalArgumentException]
            , s"Expected IllegalArgumentException, but got ${e.getClass}")

        val updatedMaxTaskTry = Task.changeMaxRepeatInterval(task, 1)
        updatedMaxTaskTry match
          case Success(t) => assert(false)
          case Failure(e) => assert(e.isInstanceOf[IllegalArgumentException]
            , s"Expected IllegalArgumentException, but got ${e.getClass}")
      }
    }
  }
}
