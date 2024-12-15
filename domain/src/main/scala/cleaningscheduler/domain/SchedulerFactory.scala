package cleaningscheduler.domain

import com.github.nscala_time.time
import com.github.nscala_time.time.Imports

class SchedulerFactory extends IScheduleFactory:
  override def createPerson(name: String, availabilityAssignment: Map[Int, Int]): Person =
    Person(name, availabilityAssignment)

  override def createTask(name: String,
                          costs: Int,
                          preferredAssignee: IPerson,
                          isPreferredFixed: Boolean,
                          lastDoneAt: Int,
                          isRepeated: Boolean,
                          minRepeatInterval: Int,
                          maxRepeatInterval: Int): Task =
    Task(name, costs, 
      preferredAssignee.asInstanceOf[Person], 
      isPreferredFixed, lastDoneAt, isRepeated, minRepeatInterval, maxRepeatInterval)

  override def createWeek(weekNumber: Int, taskAssignment: Map[_ <: ITask, _ <:IPerson]): Week =
    val taskAssignmentImpl = taskAssignment.map {case (k, v) =>
      (k.asInstanceOf[Task], v.asInstanceOf[Person])
    }
    Week(weekNumber, taskAssignmentImpl)

  override def createSchedule(createdAt: Imports.DateTime, weekList: List[IWeek]): Schedule =
    val weekListImpl = weekList.map {week => week.asInstanceOf[Week]}
    Schedule(createdAt, weekListImpl)

   

