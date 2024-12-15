package cleaningscheduler.domain.scheduler

import cleaningscheduler.domain.Schedule.calculateScoreMaxTasks
import cleaningscheduler.domain.scheduler.MaxTaskPersonVariableScheduler.scheduleMaxTasksPersonVariable
import cleaningscheduler.domain.search.{ScheduleSearcher, ScheduleSearcherOnlyPersonVariable}
import cleaningscheduler.domain.{IPerson, ISchedule, ITask, Person, Schedule, Task}
import com.github.nscala_time.time.Imports.DateTime

import java.util
import scala.jdk.CollectionConverters.*

class MaxTaskPersonVariableScheduler extends IScheduler:
  override def schedule(personList: util.List[IPerson],
                        taskList: util.List[ITask],
                        startWeek: Int,
                        scheduleLength: Int,
                        isIntervalVariable: Boolean
                       ): ISchedule =
    val personScalaList: List[Person] = personList.asScala.map(_.asInstanceOf[Person]).toList
    val taskScalaList: List[Task] = taskList.asScala.map(_.asInstanceOf[Task]).toList
    val (schedule, _) = scheduleMaxTasksPersonVariable(personScalaList, taskScalaList, startWeek, scheduleLength, 4)
    schedule

  override def calculateScore(schedule: ISchedule, personList: util.List[IPerson]): Int = this.calculateScore(schedule, 4)

  def calculateScore(schedule: ISchedule, maxTasksPerWeek: Int): Int =
    Schedule.calculateScoreMaxTasks(schedule.asInstanceOf[Schedule], maxTasksPerWeek)

  override def scheduleBalanced(personList: util.List[IPerson], 
                                taskList: util.List[ITask], 
                                startWeek: Int, 
                                scheduleLength: Int, 
                                isIntervalVariable: Boolean): ISchedule =
    this.schedule(personList, taskList, startWeek, scheduleLength, isIntervalVariable)

  override def calculateScoreBalanced(schedule: ISchedule, personList: util.List[IPerson]): Int = 
    this.calculateScore(schedule, 4) 

object MaxTaskPersonVariableScheduler:
  def scheduleMaxTasksPersonVariable(personList: List[Person],
                                     taskList: List[Task],
                                     startWeek: Int,
                                     scheduleLength: Int,
                                     maxTasksPerWeek: Int
                                    ): (Schedule, Int) =
    val defaultSchedule = Schedule.defaultSchedule(taskList, startWeek, scheduleLength, DateTime.now())

    val optionsList = ScheduleSearcherOnlyPersonVariable.getOptionsList(taskList, personList, startWeek, scheduleLength)
    val optionsTree = ScheduleSearcherOnlyPersonVariable.buildOptionTree(optionsList)
    val scheduleTree = ScheduleSearcherOnlyPersonVariable.buildScheduleTree(optionsTree, defaultSchedule)

    ScheduleSearcher.searchBestSchedule(scheduleTree, Schedule.calculateScoreMaxTasks(_, maxTasksPerWeek))

