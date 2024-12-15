package cleaningscheduler.domain.scheduler

import cleaningscheduler.domain.*
import cleaningscheduler.domain.search.ScheduleSearcher
import com.github.nscala_time.time.Imports.DateTime
import org.scalatest.wordspec.AnyWordSpec
import scala.jdk.CollectionConverters._

class MaxCostsTests extends AnyWordSpec {
  "MaxCostPerPersonScheduler" when {
    "given a schedule" should {
      "correctly calculate the score" in {
        val person = Person("PersonA", Map(30 -> 40, 31 -> 40))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 50, person, false, 25, false, 0, 0)
        val week1 = Week(30, Map(task1 -> person))
        val week2 = Week(31, Map(task2 -> person))
        val schedule = Schedule(DateTime.now(), List(week1, week2))

        val scorer = new MaxCostsPerPersonScheduler()
        val score = scorer.calculateScore(schedule, java.util.List.of(person))
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

        val scorer = new MaxCostsPerPersonScheduler
        val score = scorer.calculateScoreBalanced(schedule, List(person1, person2).asJava)
        assert(score == 90 - 15)
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

        val scheduler = new MaxCostsPerPersonScheduler

        val schedule = scheduler.schedule(personList.asJava,
          taskList.asJava, 30, 4, true)
        val score = scheduler.calculateScore(schedule, java.util.List.of(person1, person2))

        assert(score == 30)
      }
    }
  }

  "searchBestSchedule with variable person and tasks" when {
    "without variable interval but many tasks" should {
      "should not take too long" in {
        val person1 = Person("PersonA", Map(30 -> 30, 31 -> 0, 32 -> 30, 33 -> 40))
        val person2 = Person("PersonB", Map(30 -> 0, 31 -> 30, 32 -> 0, 33 -> 20))
        val person3 = Person("PersonC", Map(30 -> 0, 31 -> 30, 32 -> 0, 33 -> 20))
        val task1 = Task("task1", 30, person1, false, 29, true, 1, 2)
        val task2 = Task("task2", 30, person2, false, 28, true, 2, 2)
        val task3 = Task("task3", 30, person2, false, 28, false, 2, 2)
        val task4 = Task("task4", 30, person2, false, 28, false, 2, 2)
        val task5 = Task("task5", 30, person2, false, 28, false, 2, 2)
        val task6 = Task("task6", 30, person2, false, 28, true, 2, 2)
        val task7 = Task("task7", 30, person2, false, 28, true, 2, 2)
        val taskList = List(task1, task2, task3, task4, task5, task6, task7)
        val personList = List(person1, person2, person3)

        val date = DateTime.now()

        val scheduler = new MaxCostsPerPersonScheduler


        val startTime = System.currentTimeMillis()
        val schedule = scheduler.schedule(personList.asJava,
          taskList.asJava, 30, 4, false)
        val durationInS = (System.currentTimeMillis() - startTime) / 1_000

        assert(durationInS < 15)
      }
    }
  }

  "searchBestSchedule with balanced workload" should {
    "find a schedule with low workload deviation" in {
      val person1 = Person("PersonA", Map(30 -> 60, 31 -> 60))
      val person2 = Person("PersonB", Map(30 -> 0, 31 -> 60))
      val task1 = Task("task1", 30, person1, false, 25, true, 1, 1)
      val task2 = Task("task2", 30, person1, false, 25, true, 1, 1)
      val taskList = List(task1, task2)
      val personList = List(person1, person2)

      val date = DateTime.now()

      val scheduler = new MaxCostsPerPersonScheduler

      val schedule = scheduler.scheduleBalanced(personList.asJava,
        taskList.asJava, 30, 2, false)

      val score = scheduler.calculateScoreBalanced(schedule, personList.asJava)

      assert(score == 60)
    }
  }

  "searchBestSchedule with variable person and tasks" when {
    "without variable interval and few tasks" should {
      "should not take too long" in {
        val person1 = Person("PersonA", Map(30 -> 30, 31 -> 0, 32 -> 30, 33 -> 40))
        val person2 = Person("PersonB", Map(30 -> 0, 31 -> 30, 32 -> 0, 33 -> 20))
        val person3 = Person("PersonC", Map(30 -> 0, 31 -> 30, 32 -> 0, 33 -> 20))
        val task1 = Task("task1", 30, person1, false, 29, true, 1, 2)
        val task2 = Task("task2", 30, person2, false, 28, true, 2, 2)
        val task3 = Task("task3", 30, person2, false, 28, false, 2, 2)
        val task4 = Task("task4", 30, person2, false, 28, false, 2, 2)
        val task5 = Task("task5", 30, person2, false, 28, false, 2, 2)
        val taskList = List(task1, task2, task3, task4, task5)
        val personList = List(person1, person2, person3)

        val date = DateTime.now()

        val scheduler = new MaxCostsPerPersonScheduler


        val startTime = System.currentTimeMillis()
        val schedule = scheduler.schedule(personList.asJava,
          taskList.asJava, 30, 4, false)
        val durationInS = (System.currentTimeMillis() - startTime) / 1_000

        assert(durationInS < 15)
      }
    }
  }

}