package cleaningscheduler.domain

import com.github.nscala_time.time.Imports.DateTime
import org.scalatest.wordspec.AnyWordSpec

import java.util
import scala.jdk.CollectionConverters.*

class TestTask(name: String,
                    costs: Int,
                    isPreferredFixed: Boolean,
                    lastDoneAt: Int,
                    isRepeated: Boolean,
                    minRepeatInterval: Int,
                    maxRepeatInterval: Int
                   ) extends Task(name, costs, new TestPerson("testPerson", Map(1-> 1)),
  isPreferredFixed, lastDoneAt, isRepeated, minRepeatInterval, maxRepeatInterval)
class TestPerson(name: String,
                      availabilityAssignment: Map[Int, Int]
                     ) extends Person(name, availabilityAssignment) {
  override def getAvailabilityAssignmentAsJavaMap: util.Map[Integer, Integer] =
    this.availabilityAssignment.map { (k, v) =>
      (k.asInstanceOf[java.lang.Integer], v.asInstanceOf[java.lang.Integer])
    }.asJava
}

class SchedulerFactoryTests extends AnyWordSpec {
  "Person" when {
    "created by factory" should {
      "have the correct name" in {
        val factory = new SchedulerFactory()
        val person = factory.createPerson("PersonA", Map[Int, Int]())

        assert(person.name == "PersonA")
      }
    }
  }

  "Task" when {
    "created by factory" should {
      "have the correct name" in {
        val factory = new SchedulerFactory()
        val person = factory.createPerson("PersonA", Map[Int, Int]())
        val task = factory.createTask(
          "task1", 30, person, true, 20, true, 1, 5
        )

        assert(task.name == "task1")
      }
    }
  }

  "Week" when {
    "created by factory" should {
      "have the correct weeknumber" in {
        val factory = new SchedulerFactory()
        val week = factory.createWeek(30, Map[Task, Person]())

        assert(week.weekNumber == 30)
      }
    }
    "when taskAssignment is of the type Map[ITask, IPerson]" should {
      "cast to Task and Person" in {
        // Mock instances of ITask and IPerson that are not Task or Person
        val mockIPerson: IPerson = new TestPerson("testPerson", Map[Int, Int]())
        val mockITask: ITask = TestTask("testTask", 20, true, 1, true, 1, 5)

        // Call createWeek with the mocked ITask and IPerson
        val weekNumber = 1
        val taskAssignment: Map[ITask, IPerson] = Map(mockITask -> mockIPerson)

        // This should invoke the asInstanceOf casts in createWeek
        val factory = new SchedulerFactory()
        val result = factory.createWeek(weekNumber, taskAssignment)

        // Add assertions if necessary
        assert(result.weekNumber == weekNumber)
      }
    }
  }

  "Schedule" when {
    "created by factory" should {
      "have the correct createdAt" in {
        val date = DateTime.now()
        val factory = new SchedulerFactory()
        val schedule = factory.createSchedule(date, List())

        assert(schedule.createdAt == date)
      }
    }
  }
  
 
}
