package cleaningscheduler.domain.scheduler

import org.scalatest.wordspec.AnyWordSpec
import cleaningscheduler.domain.*
import com.github.nscala_time.time.Imports.DateTime
import scala.jdk.CollectionConverters._

class MaxTaskPersonVariableTests extends AnyWordSpec {
  "MaxTaskScheduler" when {
    "given a schedule" should {
      "correctly calculate the score" in {
        val person = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person, false, 25, false, 0, 0)
        val week1 = Week(30, Map(task1 -> person))
        val week2 = Week(31, Map(task2 -> person))
        val schedule = Schedule(DateTime.now(), List(week1, week2))

        val scorer = new MaxTaskPersonVariableScheduler()
        val score = scorer.calculateScore(schedule, java.util.List.of(person))
        assert(score == 6)
      }
    }
    "calculating balanced score" should {
      "return the same as the regular score" in {
        val person = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person, false, 25, false, 0, 0)
        val week1 = Week(30, Map(task1 -> person))
        val week2 = Week(31, Map(task2 -> person))
        val schedule = Schedule(DateTime.now(), List(week1, week2))

        val scorer = new MaxTaskPersonVariableScheduler()
        val score = scorer.calculateScore(schedule, java.util.List.of(person))
        val balancedScore = scorer.calculateScoreBalanced(schedule, java.util.List.of(person))
        assert(balancedScore == score)
      }
    }
    "given correct parameters" should {
      "find the best schedule" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person2, true, 25, true, 1, 1)
        val taskList = List(task1, task2)
        val personList = List(person1, person2)

        val date = DateTime.now()

        val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 2, date)

        val scheduler = new MaxTaskPersonVariableScheduler

        val schedule = scheduler.schedule(personList.asJava, taskList.asJava, 30, 2, false)
        val score = scheduler.calculateScore(schedule, personList.asJava)

        assert(score == scheduler.calculateScore(defaultSchedule, 4))
      }
    }
  }

  "scheduleMaxTasksPersonVariable" when {
    "scheduling for only one week" should {
      "plan all tasks in that week" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person1, false, 25, false, 2, 2)
        val task3 = Task("task3", 20, person2, false, 25, true, 1, 1)

        val (schedule, _) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
          List(person1, person2), List(task1, task2, task3), 30, 1, 5)
        val firstWeek = schedule.weekList.head

        assert(firstWeek.getTaskAssignment.size == 3)
      }

      "assign all tasks to preferred assignee" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 20, 31 -> 60))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person1, false, 25, false, 2, 2)
        val task3 = Task("task3", 20, person2, false, 25, true, 1, 1)

        val (schedule, _) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
          List(person1, person2), List(task1, task2, task3), 30, 1, 5)
        val taskAssignment = schedule.weekList.head.getTaskAssignment

        taskAssignment.get(task1) match
          case Some(p) => assert(p == person1)
          case None => fail()
        taskAssignment.get(task2) match
          case Some(p) => assert(p == person1)
          case None => fail()
        taskAssignment.get(task3) match
          case Some(p) => assert(p == person2)
          case None => fail()
      }
    }
    "task should not be repeated" should {
      "only add the task once" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 3, 3)

        val (schedule, _) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
          List(person1), List(task1), 30, 4, 5)

        val expectedWeekList = List(Week(30, Map()),
          Week(31, Map()), Week(32, Map(task1 -> person1)), Week(33, Map()))

        assert(schedule.weekList == expectedWeekList)
      }

      "with tasks for multiple weeks" should {
        "create a weekList for the length of the schedule" in {
          val (schedule, _) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(List(), List(), 30, 4, 5)
          val weekList = List(Week(30, Map()), Week(31, Map()),
            Week(32, Map()), Week(33, Map()))

          assert(schedule.weekList == weekList)
        }
      }

      "with tasks with lastDoneAt out of range of startweek" should {
        "schedule tasks in the startweek" in {
          val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
          val task1 = Task("task1", 30, person1, false, 25, true, 3, 3)
          val task2 = Task("task2", 50, person1, false, 25, true, 2, 2)

          val (schedule, _) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
            List(person1), List(task1, task2), 30, 6, 5
          )

          val expectedWeekList = List(Week(30, Map(task1 -> person1, task2 -> person1)),
            Week(31, Map()),
            Week(32, Map(task2 -> person1)),
            Week(33, Map(task1 -> person1)),
            Week(34, Map(task2 -> person1)),
            Week(35, Map()))

          assert(schedule.weekList == expectedWeekList)
        }
      }

      "with tasks with lastDoneAt within range of startweek" should {
        "schedule tasks later than the startweek" in {
          val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
          val task1 = Task("task1", 30, person1, false, 28, true, 3, 3)
          val task2 = Task("task2", 50, person1, false, 29, true, 2, 2)

          val (schedule, _) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
            List(person1), List(task1, task2), 30, 6, 5
          )

          val expectedWeekList = List(Week(30, Map()),
            Week(31, Map(task1 -> person1, task2 -> person1)),
            Week(32, Map()),
            Week(33, Map(task2 -> person1)),
            Week(34, Map(task1 -> person1)),
            Week(35, Map(task2 -> person1)))

          assert(schedule.weekList == expectedWeekList)
        }
      }

      "with plenty of time and only one task" should {
        "calculate a positive score" in {
          val person1 = Person("PersonA", Map(30 -> 40))
          val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)

          val (_, score) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
            List(person1), List(task1), 30, 1, 5
          )

          assert(score > 0)
        }
      }

      "with too many tasks for the allowed limit" should {
        "calculate a negative score " in {
          val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
          val task1 = Task("task1", 30, person1, false, 25, true, 3, 3)
          val task2 = Task("task2", 50, person1, false, 25, true, 2, 2)

          val (_, score) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
            List(person1), List(task1, task2), 30, 1, 1
          )

          assert(score < 0)
        }
      }
    }
    "scheduling balanced" should {
      "return the same schedule as the unbalanced version" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person2, true, 25, true, 1, 1)
        val taskList = List(task1, task2)
        val personList = List(person1, person2)

        val date = DateTime.now()

        val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 2, date)
        val scheduler = new MaxTaskPersonVariableScheduler

        val schedule = scheduler.schedule(
          personList.asJava, taskList.asJava, 30, 2, false)

        val balancedSchedule= scheduler.scheduleBalanced(
          personList.asJava, taskList.asJava, 30, 2, false)

        assert(schedule.weekList == balancedSchedule.weekList)
      }
    }

    "return the default schedule " in {
      val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
      val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
      val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
      val task2 = Task("task2", 30, person2, true, 25, true, 1, 1)
      val taskList = List(task1, task2)
      val personList = List(person1, person2)

      val date = DateTime.now()

      val defaultSchedule = Schedule.defaultSchedule(taskList, 30, 2, date)

      val (schedule, score) = MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable(
        personList, taskList, 30, 2, 2)

      assert(score == Schedule.calculateScoreMaxTasks(defaultSchedule, 2))
    }
  }
}
