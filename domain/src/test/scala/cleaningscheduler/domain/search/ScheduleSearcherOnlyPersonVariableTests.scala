package cleaningscheduler.domain.search

import cleaningscheduler.domain.{Person, Schedule, Task, Week, search}
import com.github.nscala_time.time.Imports.DateTime
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class ScheduleSearcherOnlyPersonVariableTests extends AnyWordSpec with Matchers {
  "getOptionsList with only person variable" when {
    "preferredAssignee is fixed" should {
      "add single element personList to the optionsList" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, true, 25, false, 0, 0)

        val optionsList = ScheduleSearcherOnlyPersonVariable.getOptionsList(
          List(task1), List(person1, person2), 30, 2)

        val expectedList = List((task1, 30, List(person1)))

        optionsList shouldBe expectedList
      }
    }
  }

  "preferredAssignee is not fixed" should {
    "add full personList to the optionsList" in {
      val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
      val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
      val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)

      val optionsList = ScheduleSearcherOnlyPersonVariable.getOptionsList(
        List(task1), List(person1, person2), 30, 2)

      val expectedList = List((task1, 30, List(person1, person2)))

      optionsList shouldBe expectedList
    }

    "relevantWeeks contains multiple weeks" should {
      "add an option for each of the weeks" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, true, 2, 2)

        val optionsList = ScheduleSearcherOnlyPersonVariable.getOptionsList(
          List(task1), List(person1), 30, 3)

        val expectedList = List((task1, 30, List(person1)),
          (task1, 32, List(person1))
        )

        optionsList shouldBe expectedList
      }
    }
  }

  "buildOptionsTree" when {
    "given a single element list with one person" should {
      "return a Tree with a null node and one childNode" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val optionsList = List((task1, 30, List(person1)))

        val tree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, 30, person1), List())
        ))

        tree shouldBe expectedTree
      }
    }

    "given a single element list with multiple persons" should {
      "return a tree with null node and multiple childNodes" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val optionsList = List((task1, 30, List(person1, person2)))

        val tree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, 30, person1), List()),
          Node((task1, 30, person2), List())
        ))

        tree shouldBe expectedTree
      }
    }

    "given multiple elements with one person each" should {
      "return a tree with null node and a chain of single childNodes" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person1, false, 25, false, 0, 0)
        val task3 = Task("task3", 30, person1, false, 25, false, 0, 0)
        val optionsList = List((task1, 30, List(person1)),
          (task2, 30, List(person1)),
          (task3, 30, List(person1)))

        val tree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, 30, person1), List(
            Node((task2, 30, person1), List(
              Node((task3, 30, person1), List())
            ))
          )),
        ))

        tree shouldBe expectedTree
      }
    }

    "given multiple elements with multiple persons each" should {
      "return a tree with multiple childNodes each having multiple other childNodes" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person1, false, 25, false, 0, 0)
        val task3 = Task("task3", 30, person1, false, 25, false, 0, 0)
        val optionsList = List((task1, 30, List(person1, person2)),
          (task2, 30, List(person1, person2)),
          (task3, 30, List(person1, person2)))

        val tree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, 30, person1), List(
            Node((task2, 30, person1), List(
              Node((task3, 30, person1), List()),
              Node((task3, 30, person2), List())
            )),
            Node((task2, 30, person2), List(
              Node((task3, 30, person1), List()),
              Node((task3, 30, person2), List())
            ))
          )),
          Node((task1, 30, person2), List(
            Node((task2, 30, person1), List(
              Node((task3, 30, person1), List()),
              Node((task3, 30, person2), List())
            )),
            Node((task2, 30, person2), List(
              Node((task3, 30, person1), List()),
              Node((task3, 30, person2), List())
            ))
          )),
        ))

        tree shouldBe expectedTree
      }
    }
  }

  "buildScheduleTree with only person variable" should {
    "construct a tree with schedules as nodes" in {
      val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
      val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
      val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
      val task2 = Task("task2", 30, person2, true, 25, true, 1, 1)
      val taskList = List(task1, task2)
      val personList = List(person1, person2)

      val optionsList = ScheduleSearcherOnlyPersonVariable.getOptionsList(taskList, personList, 30, 2)
      val optionTree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)
      val date = DateTime.now()

      val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 2, date)
      val week30leaf1 = Week(30, Map(task1 -> person1, task2 -> person2))
      val week31leaf1 = Week(31, Map(task1 -> person1))
      val schedule1 = Schedule(date, List(week30leaf1, week31leaf1))

      val week30leaf2 = Week(30, Map(task1 -> person1, task2 -> person2))
      val week31leaf2 = Week(31, Map(task1 -> person1))
      val schedule2 = Schedule(date, List(week30leaf2, week31leaf2))

      val scheduleTree = ScheduleSearcherOnlyPersonVariable.buildScheduleTree(optionTree, defaultSchedule)

      var leafCounter = 1

      scheduleTree match
        case Node(value, List()) if leafCounter == 1 =>
          leafCounter += 1
          value shouldBe schedule1
        case Node(value, List()) if leafCounter == 2 =>
          value shouldBe schedule2
        case _ => ()
    }
  }

  "searchBestSchedule with only person variable" when {
    "given the max tasks cost function" should {
      "return the default schedule " in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person2, true, 25, true, 1, 1)
        val taskList = List(task1, task2)
        val personList = List(person1, person2)

        val date = DateTime.now()

        val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 2, date)
        val optionsList = ScheduleSearcherOnlyPersonVariable.getOptionsList(taskList, personList, 30, 2)
        val optionsTree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)
        val scheduleTree = ScheduleSearcherOnlyPersonVariable.buildScheduleTree(optionsTree, defaultSchedule)

        val (schedule, score) = ScheduleSearcher.searchBestSchedule(
          scheduleTree, Schedule.calculateScoreMaxTasks(_, 2))

        assert(schedule == defaultSchedule)
        assert(score == Schedule.calculateScoreMaxTasks(defaultSchedule, 2))
      }
    }
    "given the costs per person per week function" should {
      "return the best schedule" in {
        val person1 = Person("PersonA", Map(30 -> 10, 31 -> 10))
        val person2 = Person("PersonB", Map(30 -> 60, 31 -> 60))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person1, false, 25, true, 1, 1)
        val taskList = List(task1, task2)
        val personList = List(person1, person2)

        val date = DateTime.now()

        val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 2, date)
        val optionsList = ScheduleSearcherOnlyPersonVariable.getOptionsList(taskList, personList, 30, 2)
        val optionsTree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)
        val scheduleTree = ScheduleSearcherOnlyPersonVariable.buildScheduleTree(optionsTree, defaultSchedule)

        val (schedule, score) = ScheduleSearcher.searchBestSchedule(
          scheduleTree, Schedule.calculateScore(_, personList))

        assert(schedule != defaultSchedule)
        assert(score == 50)
      }
    }
  }
}
