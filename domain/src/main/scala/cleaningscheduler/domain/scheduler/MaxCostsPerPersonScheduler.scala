package cleaningscheduler.domain.scheduler

import cleaningscheduler.domain.scheduler.MaxCostsPerPersonScheduler.scheduleWithCostFunction
import cleaningscheduler.domain.search.ScheduleSearcher
import cleaningscheduler.domain.{IPerson, ISchedule, ITask, Person, Schedule, Task}
import com.github.nscala_time.time.Imports.DateTime

import java.util
import scala.jdk.CollectionConverters.*

class MaxCostsPerPersonScheduler extends IScheduler:
  override def calculateScore(schedule: ISchedule, personList: util.List[IPerson]): Int =
    val personScalaList: List[Person] = personList.asScala.map(_.asInstanceOf[Person]).toList
    Schedule.calculateScore(schedule.asInstanceOf[Schedule], personScalaList)

  override def schedule(personList: util.List[IPerson],
                        taskList: util.List[ITask],
                        startWeek: Int,
                        scheduleLength: Int,
                        isIntervalVariable: Boolean
                       ): ISchedule =
    val personScalaList: List[Person] = personList.asScala.map(_.asInstanceOf[Person]).toList
    val taskScalaList: List[Task] = taskList.asScala.map(_.asInstanceOf[Task]).toList

    scheduleWithCostFunction(personScalaList, taskScalaList, startWeek, scheduleLength, isIntervalVariable,
      Schedule.calculateScore(_, personScalaList)
    )

  override def scheduleBalanced(personList: util.List[IPerson],
                                taskList: util.List[ITask],
                                startWeek: Int,
                                scheduleLength: Int,
                                isIntervalVariable: Boolean): ISchedule =
    val personScalaList: List[Person] = personList.asScala.map(_.asInstanceOf[Person]).toList
    val taskScalaList: List[Task] = taskList.asScala.map(_.asInstanceOf[Task]).toList

    scheduleWithCostFunction(personScalaList, taskScalaList, startWeek, scheduleLength, isIntervalVariable,
      Schedule.calculateScoreBalanced(_, personScalaList)
    )

  override def calculateScoreBalanced(schedule: ISchedule, personList: util.List[IPerson]): Int =
    val personScalaList: List[Person] = personList.asScala.map(_.asInstanceOf[Person]).toList
    Schedule.calculateScoreBalanced(schedule.asInstanceOf[Schedule], personScalaList)

object MaxCostsPerPersonScheduler:
  def scheduleWithCostFunction(personList: List[Person],
                               taskList: List[Task],
                               startWeek: Int,
                               scheduleLength: Int,
                               isIntervalVariable: Boolean,
                               costFunction: Schedule => Int
                              ): ISchedule =

    val quickVersion = (isIntervalVariable && ((scheduleLength > 4) || (personList.length + taskList.length >= 7)))
      || (!isIntervalVariable && ((scheduleLength > 6) || (personList.length + taskList.length >= 10)))

    val defaultSchedule = Schedule.defaultSchedule(taskList, startWeek, scheduleLength, DateTime.now())

    val optionsList = ScheduleSearcher.getOptionsList(taskList, personList,
      startWeek, scheduleLength, quickVersion, isIntervalVariable)
    val optionsTree = ScheduleSearcher.buildOptionTree(optionsList)
    val scheduleTree = ScheduleSearcher.buildScheduleTree(optionsTree, defaultSchedule)

    val (schedule, _) = ScheduleSearcher.searchBestSchedule(scheduleTree, costFunction)
    schedule
