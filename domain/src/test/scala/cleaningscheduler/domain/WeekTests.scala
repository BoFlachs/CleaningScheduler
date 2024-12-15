package cleaningscheduler.domain

import org.scalatest.wordspec.AnyWordSpec

class WeekTests extends AnyWordSpec {
  "Week" when {
    "constructed" should {
      "have week number" in {
        val week = Week(30, Map[Task, Person]())
        assert(week.weekNumber === 30)
      }
      "have taskAssignment" in {
        val week = Week(30, Map[Task, Person]())
        assert(week.getTaskAssignment === Map[Task, Person]())
      }
      "have a taskAssignment as java map" in {
        val week = Week(30, Map[Task, Person]())
        val aaAsJava = week.getTaskAssignmentAsJava
        assert(aaAsJava.isInstanceOf[java.util.Map[ITask, IPerson]])
      }
    }
    "changed" should {
      "return new week with new task assignment" in {
        val week = Week(30, Map[Task, Person]())
        val person = Person("PersonA", Map[Int, Int]())
        val task = Task("task1", 30, person, false, 25, false, 0, 0)
        val updatedWeek = Week.changeTaskAssignment(week, Map(task -> person))
        assert(week !== updatedWeek)
        assert(updatedWeek.getTaskAssignment == Map(task -> person))
      }
    }
  }

  "Max tasks costs" when {
    "max tasks is more than number of tasks" should {
     "be the difference between them" in {
       val person = Person("PersonA", Map(30 -> 40))
       val task = Task("task1", 30, person, false, 25, false, 0, 0)
       val week = Week(30, Map(task -> person))
       val costs = Week.calculateScoreMaxTasks(week, 2)

       assert(costs == 1)
     }
    }
    "there are more scheduled tasks than number of tasks" should {
      "be the negative squared difference between them" in {
        val person = Person("PersonA", Map(30 -> 40))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person, false, 25, false, 0, 0)
        val task3 = Task("task3", 40, person, false, 25, false, 0, 0)
        val week = Week(30, Map(task1 -> person,
          task2 -> person,
          task3 -> person))
        val costs = Week.calculateScoreMaxTasks(week, 1)

        assert(costs == -4)
      }
    }
  }

  "Costs" when {
    "one task and one person" should {
      "be absolute difference between cost (c) and availability (a) if a >= c" in {
        val person = Person("PersonA", Map(30 -> 40))
        val task = Task("task1", 30, person, false, 25, false, 0, 0)
        val week = Week(30, Map(task -> person))
        val costs = Week.calculateScore(week, List(person))

        assert(costs == 10)
      }
      "be multiply the difference between costs (c) and availability (a) if a < c" in {
        val person = Person("PersonA", Map(30 -> 20))
        val task = Task("task1", 30, person, false, 25, false, 0, 0)
        val week = Week(30, Map(task -> person))
        val costs = Week.calculateScore(week, List(person))

        assert(costs == -100)
      }
      "be multiply the costs if availability is not defined for that week" in {
        val person = Person("PersonA", Map(1 -> 20))
        val task = Task("task1", 30, person, false, 25, false, 0, 0)
        val week = Week(30, Map(task -> person))
        val costs = Week.calculateScore(week, List(person))

        assert(costs == -900)
      }
    }
    "two tasks and one person" should {
      "calculate total score and multiply negative impacts on the score" in {
        val person = Person("PersonA", Map(30 -> 50))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person, false, 25, false, 0, 0)
        val week = Week(30, Map(task1 -> person, task2 -> person))
        val costs = Week.calculateScore(week, List(person))

        assert(costs == -100)
      }
      "be zero if the timing is exactly correct" in {
        val person = Person("PersonA", Map(30 -> 60))
        val task1 = Task("task1", 30, person, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person, false, 25, false, 0, 0)
        val week = Week(30, Map(task1 -> person, task2 -> person))
        val costs = Week.calculateScore(week, List(person))

        assert(costs == 0)
      }
    }
    "two tasks and two persons" should {
      "calculate total score and multiply negative impacts on the score" in {
        val person1 = Person("PersonA", Map(30 -> 50))
        val person2 = Person("PersonB", Map(30 -> 20))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person2, false, 25, false, 0, 0)
        val week = Week(30, Map(task1 -> person1, task2 -> person2))
        val costs = Week.calculateScore(week, List(person1, person2))

        assert(costs == -80)
      }
    }
    "three tasks and two persons" should {
      "calculate total score and multiply negative impacts on the score" in {
        val person1 = Person("PersonA", Map(30 -> 50))
        val person2 = Person("PersonB", Map(30 -> 20))
        val task1 = Task("task1", 30, person1, false, 25, false, 0, 0)
        val task2 = Task("task2", 30, person2, false, 25, false, 0, 0)
        val task3 = Task("task3", 40, person2, false, 25, false, 0, 0)
        val week = Week(30, Map(task1 -> person1,
          task2 -> person2,
          task3 -> person1))
        val costs = Week.calculateScore(week, List(person1, person2))

        assert(costs == -500)
      }
    }
  }

}
