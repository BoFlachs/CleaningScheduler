package cleaningscheduler.domain

import com.github.nscala_time.time.Imports.DateTime

trait IScheduleFactory {
  def createPerson(name: String, availabilityAssignment: Map[Int, Int]): IPerson
  
  def createTask(name: String,
                  costs: Int,
                  preferredAssignee: IPerson,
                  isPreferredFixed: Boolean,
                  lastDoneAt: Int,
                  isRepeated: Boolean,
                  minRepeatInterval: Int,
                  maxRepeatInterval: Int
                ): ITask
  
  def createWeek(weekNumber: Int, taskAssignment: Map[_ <: ITask, _ <: IPerson]): IWeek
  
  def createSchedule(createdAt: DateTime, weekList: List[IWeek]): ISchedule
}
