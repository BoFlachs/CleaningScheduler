package cleaningscheduler.domain

import cleaningscheduler.domain.scheduler.MaxTaskPersonVariableScheduler
import org.scalatest.wordspec.AnyWordSpec
import com.github.nscala_time.time.Imports.DateTime

class ScheduleTests extends AnyWordSpec {
  "Schedule" when {
    "constructed" should {
      "have correct date" in {
        val date = DateTime.now()
        val schedule = Schedule(date, Nil)

        assert(schedule.createdAt == date)
      }
      "have weekListAsJava to convert weeklist" in {
        val person = Person("PersonA", Map(30 -> 40))
        val task = Task("task1", 30, person, false, 25, false, 0, 0)
        val week = Week(30, Map(task -> person))
        val schedule = Schedule(DateTime.now(), List(week))

        assert(schedule.weekListAsJava.isInstanceOf[java.util.List[IWeek]])
      }
    }
  }

  "Costs" when {
    "one week" in {
      val person = Person("PersonA", Map(30 -> 40))
      val task = Task("task1", 30, person, false, 25, false, 0, 0)
      val week = Week(30, Map(task -> person))
      val schedule = Schedule(DateTime.now(), List(week))

      val score = Schedule.calculateScore(schedule, List(person))
      assert(score == 10)
    }
    "two weeks" in {
      val person = Person("PersonA", Map(30 -> 40, 31 -> 40))
      val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
      val task2 = Task("task2", 50, person, false, 25, false, 0, 0)
      val week1 = Week(30, Map(task1 -> person))
      val week2 = Week(31, Map(task2 -> person))
      val schedule = Schedule(DateTime.now(), List(week1, week2))

      val score = Schedule.calculateScore(schedule, List(person))
      assert(score == -90)
    }
  }

  "Costs with balanced workload" should {
    "be the regular score minus standard deviation of workload" in {
      val person1 = Person("PersonA", Map(30 -> 60, 31 -> 40))
      val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
      val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
      val task2 = Task("task2", 30, person2, false, 25, false, 0, 0)
      val week1 = Week(30, Map(task1 -> person1, task2 -> person1))
      val week2 = Week(31, Map(task2 -> person2))
      val schedule = Schedule(DateTime.now(), List(week1, week2))

      val score = Schedule.calculateScoreBalanced(schedule, List(person1, person2))
      assert(score == 90 - 15)
    }
  }

  "Costs max tasks" when {
    "one week" in {
      val person = Person("PersonA", Map(30 -> 40))
      val task = Task("task1", 30, person, false, 25, false, 0, 0)
      val week = Week(30, Map(task -> person))
      val schedule = Schedule(DateTime.now(), List(week))

      val score = Schedule.calculateScoreMaxTasks(schedule, 2)
      assert(score == 1)
    }
    "two weeks" in {
      val person = Person("PersonA", Map(30 -> 40, 31 -> 40))
      val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
      val task2 = Task("task2", 50, person, false, 25, false, 0, 0)
      val task3 = Task("task3", 50, person, false, 25, false, 0, 0)
      val week1 = Week(30, Map(task1 -> person))
      val week2 = Week(31, Map(task1 -> person,
        task2 -> person, task3 -> person))
      val schedule = Schedule(DateTime.now(), List(week1, week2))

      val score = Schedule.calculateScoreMaxTasks(schedule, 1)
      assert(score == -4)
    }
  }


  "changeWeek" when {
    "weekNumber is not in list" should {
      "return None" in {
        val person = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person, false, 25, false, 0, 0)
        val week1 = Week(30, Map(task1 -> person))
        val week2 = Week(31, Map(task2 -> person))
        val date = DateTime.now()
        val schedule = Schedule(date, List(week1, week2))

        assertThrows[IllegalArgumentException] {
          Schedule.changeWeek(schedule, 32, task1, person)
        }
      }
    }

    "when week is in list" should {
      "return the updated schedule" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person1, false, 25, false, 0, 0)
        val week1 = Week(30, Map(task1 -> person1))
        val week2 = Week(31, Map(task2 -> person1))
        val date = DateTime.now()
        val schedule = Schedule(date, List(week1, week2))

        val updatedSchedule = Schedule.changeWeek(schedule,
          30, task1, person2)

        val updatedWeek1 = Week(30, Map(task1 -> person2))
        val expectedUpdatedSchedule = Schedule(date, List(updatedWeek1, week2))

        assert(updatedSchedule == expectedUpdatedSchedule)
      }
    }
  }

  "reassignTask" when {
    "given an empty weekOption list" should {
      "remove all assignments of the given task" in {
        val person = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person, false, 25, false, 0, 0)
        val week1 = Week(30, Map(task1 -> person))
        val week2 = Week(31, Map(task1 -> person, task2 -> person))
        val date = DateTime.now()
        val schedule = Schedule(date, List(week1, week2))

        val updatedSchedule = Schedule.reassignTask(schedule, task1, Nil)

        val updatedWeek1 = Week(30, Map())
        val updatedWeek2 = Week(31, Map(task2 -> person))
        val expectedUpdatedSchedule = Schedule(date, List(updatedWeek1, updatedWeek2))

        assert(updatedSchedule == expectedUpdatedSchedule)
      }
    }

    "given a weekOption list" should {
      "replace all current assignments of the task by the given ones" in {
        val person1 = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val person2 = Person("PersonB", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person1, false, 25, false, 0, 0)
        val week1 = Week(30, Map(task1 -> person1))
        val week2 = Week(31, Map(task1 -> person1, task2 -> person1))
        val date = DateTime.now()
        val schedule = Schedule(date, List(week1, week2))

        val weekOption = List((30, person2))
        val updatedSchedule = Schedule.reassignTask(schedule, task1, weekOption)

        val updatedWeek1 = Week(30, Map(task1 -> person2))
        val updatedWeek2 = Week(31, Map(task2 -> person1))
        val expectedUpdatedSchedule = Schedule(date, List(updatedWeek1, updatedWeek2))

        assert(updatedSchedule == expectedUpdatedSchedule)
      }
    }
  }
}
