package cleaningscheduler.domain.search

import cleaningscheduler.domain.{Person, Schedule, Task, Week}
import com.github.nscala_time.time.Imports.DateTime
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class ScheduleSearcherTests extends AnyWordSpec with Matchers {
  "listOfRelevantWeekSequences" when {
    "task is not repeated" should {
      "return a single element sequences from start to maxRepeatInterval" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 25, false, 0, 3)

        val weekSeq = ScheduleSearcher.listOfRelevantWeekSequences(task1, 30, 6)

        val expectedSeq = Seq(Seq(30), Seq(31), Seq(32), Seq(33))

        weekSeq shouldBe expectedSeq
      }
    }
    "task is not repeated and maxRepeatInterval lies outside of scheduleLength" should {
      "return single element sequences within scheduleLength" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 25, false, 0, 3)

        val weekSeq = ScheduleSearcher.listOfRelevantWeekSequences(task1, 30, 2)

        val expectedSeq = Seq(Seq(30), Seq(31))

        weekSeq shouldBe expectedSeq
      }
    }
    "task is repeated and min/maxRepeat interval are equal" +
      "and lastDoneAt is outside of range" should {
      "return a single sequence" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 25, true, 2, 2)

        val weekSeq = ScheduleSearcher.listOfRelevantWeekSequences(task1, 30, 6)

        val expectedSeq = Seq(Seq(30, 32, 34))

        weekSeq shouldBe expectedSeq
      }
    }
    "task is repeated and min/maxRepeat interval are equal" +
      "and lastDoneAt is within range" should {
      "return a single sequence" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 29, true, 2, 2)

        val weekSeq = ScheduleSearcher.listOfRelevantWeekSequences(task1, 30, 6)

        val expectedSeq = Seq(Seq(31, 33, 35))

        weekSeq shouldBe expectedSeq
      }
    }
    "task is repeated and lastDoneAt outside of range" should {
      "return all possible sequences within scheduleLength " +
        "Note: it ignores sequences that skip the final repetition" +
        " because it would fall outside of the scheduleLength (like" +
        " (30, 32) and (30, 33) in this example" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 25, true, 2, 4)

        val weekSeq = ScheduleSearcher.listOfRelevantWeekSequences(task1, 30, 6)

        val expectedSeq = Seq(Seq(30, 32, 34), Seq(30, 32, 35),
          Seq(30, 32), Seq(30, 33, 35), Seq(30, 33), Seq(30, 34))

        weekSeq shouldBe expectedSeq
      }
    }
  }
  "task is repeated and lastDoneAt within range" should {
    "return all possible sequences within scheduleLength " +
      "Note: it ignores sequences that skip the final repetition" +
      " because it would fall outside of the scheduleLength (like" +
      " (30, 32) and (30, 33) in this example" in {
      val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
      val task1 = Task("task1", 30, person1, true, 29, true, 2, 4)

      val weekSeq = ScheduleSearcher.listOfRelevantWeekSequences(task1, 30, 4)

      val expectedSeq = Seq(Seq(31, 33), Seq(31),
        Seq(32), Seq(33))

      weekSeq shouldBe expectedSeq
    }
  }

  "getOptionsList" when {
    "single task with one person no repeat" should {
      "return a list with a single option" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 25, false, 0, 0)

        val optionsList = ScheduleSearcher.getOptionsList(
          List(task1), List(person1), 30, 4)

        val expectedOptionsList = List(
          (task1, List(
            List(
              (30, person1))
          )))

        optionsList shouldBe expectedOptionsList
      }
    }
    "single task with two persons and repeat with" +
      " min/max repeat interval equal" should {
      "return a list with same weeks but different assignees" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, true, 2, 2)

        val optionsList = ScheduleSearcher.getOptionsList(
          List(task1), List(person1, person2), 30, 4, false)

        val expectedOptionsList = List(
          (task1, List(
            List((30, person1), (32, person1)),
            List((30, person1), (32, person2)),
            List((30, person2), (32, person1)),
            List((30, person2), (32, person2)),
          )))

        optionsList shouldBe expectedOptionsList
      }
    }
    "assigned person is fixed" should {
      "assign task to that person only" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 25, true, 2, 2)

        val optionsList = ScheduleSearcher.getOptionsList(
          List(task1), List(person1, person2), 30, 4)

        val expectedOptionsList = List(
          (task1, List(
            List((30, person1), (32, person1))
          )))

        optionsList shouldBe expectedOptionsList
      }
      "isIntervalVariable is false" should {
        "select minRepeatInterval as interval" in {
          val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
          val task1 = Task("task1", 30, person1, true, 25, true, 1, 3)

          val optionsList = ScheduleSearcher.getOptionsList(
            List(task1), List(person1), 30, 4, isIntervalVariable = false)

          val expectedOptionsList = List(
            (task1, List(
              List((30, person1), (31, person1), (32, person1), (33, person1))
            )))

          optionsList shouldBe expectedOptionsList
        }
      }
    }

    //    "number of options is larger than 100" should {
    //      "pick 100 random options" in {
    //        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
    //        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
    //        val person3 = Person("PersonC", Map(30 -> 40, 31 -> 40))
    //        val task1 = Task("task1", 30, person1, false, 25, true, 2, 2)
    //
    //        val optionsList = ScheduleSearcher.getOptionsList(
    //          List(task1), List(person1, person2, person3), 30, 20)
    //
    //        optionsList.head._2.length shouldBe 100
    //      }
    //    }
  }

  "buildOptionTree" when {
    "given a single element list with one week and one person" should {
      "return a tree with one child node" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val optionsList = List(
          (task1, List(
            List((30, person1))
          )))

        val tree = ScheduleSearcher.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, List((30, person1))), List())
        ))

        tree shouldBe expectedTree
      }
    }

    "given a single element list with one week and multiple persons" should {
      "return a tree with null node and multiple childNodes" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val optionsList = List(
          (task1, List(
            List((30, person1)),
            List((30, person2))
          )))

        val tree = ScheduleSearcher.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, List((30, person1))), List()),
          Node((task1, List((30, person2))), List()),
        ))

        tree shouldBe expectedTree
      }
    }

    "given a single element list with multiple weeks and multiple persons" should {
      "return a tree with null node and multiple childNodes" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, false, 25, true, 2, 2)
        val optionsList = List(
          (task1, List(
            List((30, person1), (32, person1)),
            List((30, person2), (32, person2)),
            List((30, person1), (32, person2)),
            List((30, person2), (32, person2)),
          )))

        val tree = ScheduleSearcher.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, List((30, person1), (32, person1))), List()),
          Node((task1, List((30, person2), (32, person2))), List()),
          Node((task1, List((30, person1), (32, person2))), List()),
          Node((task1, List((30, person2), (32, person2))), List()),
        ))

        tree shouldBe expectedTree
      }
    }

    "given multiple tasks with multiple weeks and multiple persons" should {
      "return a tree with null node, multiple childNodes, each containing multiple" +
        "childNodes" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, false, 25, true, 2, 2)
        val task2 = Task("task2", 30, person2, false, 25, false, 0, 0)
        val optionsList = List(
          (task1, List(
            List((30, person1), (32, person1)),
            List((30, person2), (32, person2)),
            List((30, person1), (32, person2)),
            List((30, person2), (32, person2)),
          )),
          (task2, List(
            List((30, person1)),
            List((30, person2)),
          )),
        )

        val tree = ScheduleSearcher.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, List((30, person1), (32, person1))), List(
            Node((task2, List((30, person1))), List()),
            Node((task2, List((30, person2))), List()),
          )),
          Node((task1, List((30, person2), (32, person2))), List(
            Node((task2, List((30, person1))), List()),
            Node((task2, List((30, person2))), List()),
          )),
          Node((task1, List((30, person1), (32, person2))), List(
            Node((task2, List((30, person1))), List()),
            Node((task2, List((30, person2))), List()),
          )),
          Node((task1, List((30, person2), (32, person2))), List(
            Node((task2, List((30, person1))), List()),
            Node((task2, List((30, person2))), List()),
          )),
        ))

        tree shouldBe expectedTree
      }
    }

    "given multiple tasks with multiple possible weekSchedules and multiple persons" should {
      "return a tree with null node, multiple childNodes, each containing multiple" +
        "childNodes" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, true, 25, true, 1, 2)
        val task2 = Task("task2", 30, person2, false, 25, false, 0, 0)
        val optionsList = List(
          (task1, List(
            List((30, person1), (31, person1), (32, person1)),
            List((30, person1), (32, person1)),
          )),
          (task2, List(
            List((30, person1)),
            List((30, person2)),
          )),
        )

        val tree = ScheduleSearcher.buildOptionTree(optionsList)

        val expectedTree = Node(null, List(
          Node((task1, List((30, person1), (31, person1), (32, person1))), List(
            Node((task2, List((30, person1))), List()),
            Node((task2, List((30, person2))), List()),
          )),
          Node((task1, List((30, person1), (32, person1))), List(
            Node((task2, List((30, person1))), List()),
            Node((task2, List((30, person2))), List()),
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
      val task1 = Task("task1", 30, person1, true, 25, true, 1, 2)
      val task2 = Task("task2", 30, person2, false, 25, false, 0, 0)
      val taskList = List(task1, task2)
      val personList = List(person1, person2)

      val optionsList = ScheduleSearcher.getOptionsList(taskList, personList, 30, 3)
      val optionTree = ScheduleSearcher.buildOptionTree(optionsList)
      val date = DateTime.now()

      val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 3, date)
      val week30leaf1 = Week(30, Map(task1 -> person1, task2 -> person1))
      val week31leaf1 = Week(31, Map(task1 -> person1))
      val week32leaf1 = Week(31, Map(task1 -> person1))
      val schedule1 = Schedule(date, List(week30leaf1, week31leaf1))

      val week30leaf2 = Week(30, Map(task1 -> person1, task2 -> person2))
      val week31leaf2 = Week(31, Map(task1 -> person1))
      val week32leaf2 = Week(31, Map(task1 -> person1))
      val schedule2 = Schedule(date, List(week30leaf2, week31leaf2))

      val week30leaf3 = Week(30, Map(task1 -> person1, task2 -> person1))
      val week31leaf3 = Week(31, Map())
      val week32leaf3 = Week(31, Map(task1 -> person1))
      val schedule3 = Schedule(date, List(week30leaf2, week31leaf2))

      val week30leaf4 = Week(30, Map(task1 -> person1, task2 -> person2))
      val week31leaf4 = Week(31, Map())
      val week32leaf4 = Week(31, Map(task1 -> person1))
      val schedule4 = Schedule(date, List(week30leaf2, week31leaf2))

      val scheduleTree = ScheduleSearcher.buildScheduleTree(optionTree, defaultSchedule)

      var leafCounter = 1

      scheduleTree match
        case Node(value, List()) if leafCounter == 1 =>
          leafCounter += 1
          value shouldBe schedule1
        case Node(value, List()) if leafCounter == 2 =>
          leafCounter += 1
          value shouldBe schedule2
        case Node(value, List()) if leafCounter == 3 =>
          leafCounter += 1
          value shouldBe schedule3
        case Node(value, List()) if leafCounter == 4 =>
          value shouldBe schedule4
        case _ => ()
    }
  }

  "searchBestSchedule with variable person and tasks" when {
    "given the max tasks per week function" should {
      "return the best schedule" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, true, 29, true, 1, 2)
        val task2 = Task("task2", 30, person2, true, 28, true, 2, 2)
        val taskList = List(task1, task2)
        val personList = List(person1, person2)

        val date = DateTime.now()

        val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 4, date)
        val optionsList = ScheduleSearcher.getOptionsList(taskList, personList, 30, 4)
        val optionsTree = ScheduleSearcher.buildOptionTree(optionsList)
        val scheduleTree = ScheduleSearcher.buildScheduleTree(optionsTree, defaultSchedule)

        val (schedule, score) = ScheduleSearcher.searchBestSchedule(
          scheduleTree, Schedule.calculateScoreMaxTasks(_, 1))

        assert(schedule != defaultSchedule)
        assert(score == 0)
      }
    }
  }


  "searchBestSchedule with variable person and tasks" when {
    "given costs pp per week function" should {
      "return the best schedule" in {
        val person1 = Person("PersonA", Map(30 -> 30, 31 -> 0, 32 -> 30, 33 -> 40))
        val person2 = Person("PersonB", Map(30 -> 0, 31 -> 30, 32 -> 0, 33 -> 20))
        val task1 = Task("task1", 30, person1, false, 29, true, 1, 2)
        val task2 = Task("task2", 30, person2, false, 28, true, 2, 2)
        val taskList = List(task1, task2)
        val personList = List(person1, person2)

        val date = DateTime.now()

        val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 4, date)
        val optionsList = ScheduleSearcher.getOptionsList(taskList, personList, 30, 4, false)
        val optionsTree = ScheduleSearcher.buildOptionTree(optionsList)
        val scheduleTree = ScheduleSearcher.buildScheduleTree(optionsTree, defaultSchedule)

        val (schedule, score) = ScheduleSearcher.searchBestSchedule(
          scheduleTree, Schedule.calculateScore(_, personList))

        assert(schedule != defaultSchedule)
        assert(score == 30)
      }
    }
  }

  "getAllCombinedOptions" should {
    "produce the cartesian product of task assignments" in {
      val person1 = Person("PersonA", Map(30 -> 30, 31 -> 0, 32 -> 30, 33 -> 40))
      val person2 = Person("PersonB", Map(30 -> 0, 31 -> 30, 32 -> 0, 33 -> 20))
      val task1 = Task("task1", 30, person1, false, 29, true, 2, 2)
      val task2 = Task("task2", 30, person2, false, 28, true, 2, 2)
      val task3 = Task("task3", 30, person2, false, 28, true, 2, 2)
      val taskList = List(task1, task2, task3)
      val personList = List(person1, person2)

      val optionsList = ScheduleSearcher.getOptionsList(taskList, personList, 30, 2, false)
      val combinedOptions = ScheduleSearcher.getAllCombinedOptions(optionsList)

      combinedOptions.length shouldBe 8
    }
  }

  "searchBestSchedule with variable person and tasks" when {
    "many tasks and people and quickVersion" should {
      "not break down" in {
        val person1 = Person("PersonA", Map(30 -> 30, 31 -> 0, 32 -> 30, 33 -> 40))
        val person2 = Person("PersonB", Map(30 -> 0, 31 -> 30, 32 -> 0, 33 -> 20))
        val person3 = Person("PersonC", Map(30 -> 40, 31 -> 30, 32 -> 0, 33 -> 120))
        val person4 = Person("PersonD", Map(30 -> 30, 31 -> 30, 32 -> 80, 33 -> 20))
        val person5 = Person("PersonE", Map(30 -> 20, 31 -> 60, 32 -> 0, 33 -> 20))
        val task1 = Task("task1", 30, person1, false, 29, true, 1, 2)
        val task2 = Task("task2", 30, person2, false, 28, false, 2, 2)
        val task3 = Task("task3", 60, person2, true, 25, true, 1, 3)
        val task4 = Task("task4", 20, person1, true, 28, false, 2, 3)
        val task5 = Task("task5", 90, person2, false, 1, true, 2, 4)
        val taskList = List(task1, task2, task3, task4)
        val personList = List(person1, person2, person3)

        val date = DateTime.now()

        val scheduleLength = 4
        val defaultSchedule = Schedule.defaultSchedule(taskList, 30, scheduleLength, date)
        val optionsList = ScheduleSearcher.getOptionsList(taskList, personList, 30, scheduleLength)
        val optionsTree = ScheduleSearcher.buildOptionTree(optionsList)
        val scheduleTree = ScheduleSearcher.buildScheduleTree(optionsTree, defaultSchedule)

        val startTime = System.currentTimeMillis()
        val (schedule, score) = ScheduleSearcher.searchBestSchedule(
          scheduleTree, Schedule.calculateScore(_, personList))
        val durationInS = (System.currentTimeMillis() - startTime) / 1_000

        assert(durationInS < 15)
      }
    }
  }

}