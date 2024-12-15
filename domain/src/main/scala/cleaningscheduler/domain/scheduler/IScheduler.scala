package cleaningscheduler.domain.scheduler

import cleaningscheduler.domain.{IPerson, ISchedule, ITask}
import java.util

trait IScheduler {
  def schedule(personList: util.List[IPerson],
               taskList: util.List[ITask], 
               startWeek: Int,
               scheduleLength: Int,
               isIntervalVariable: Boolean
              ): ISchedule
  
  def calculateScore(schedule: ISchedule, personList: util.List[IPerson]): Int

  def scheduleBalanced(personList: util.List[IPerson],
               taskList: util.List[ITask],
               startWeek: Int,
               scheduleLength: Int,
               isIntervalVariable: Boolean
              ): ISchedule

  def calculateScoreBalanced(schedule: ISchedule, personList: util.List[IPerson]): Int

}
