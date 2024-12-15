package cleaningscheduler.domain

import com.github.nscala_time.time.Imports.DateTime

import java.util
import scala.annotation.tailrec
import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps

case class Schedule(createdAt: DateTime,
                    weekList: List[Week]
                   ) extends ISchedule {
  override def weekListAsJava: util.List[IWeek] = this.weekList.asJava
}

object Schedule:
  protected[domain] def changeWeek(schedule: Schedule,
                                   weekNumber: Int,
                                   task: Task,
                                   person: Person): Schedule =
    val relevantWeekList = schedule.weekList.zipWithIndex.filter((week, index) => week.weekNumber == weekNumber)
    if relevantWeekList.isEmpty then
      throw new IllegalArgumentException("Can't find week " + weekNumber + " in the schedule")
    else
      val index = relevantWeekList.head._2
      val updatedWeek = relevantWeekList.head._1.copy(
        taskAssignment = relevantWeekList.head._1.taskAssignment.updated(task, person))
      schedule.copy(weekList = schedule.weekList.updated(index, updatedWeek))

  protected[domain] def reassignTask(schedule: Schedule,
                                     task: Task,
                                     weekOption: List[(Int, Person)]): Schedule =
    val scheduleWithTaskRemoved = schedule.copy(weekList = schedule.weekList.map { week =>
      week.copy(taskAssignment = week.taskAssignment.filter {
        (assignedTask, _) => assignedTask != task
      })
    }
    )
    weekOption.foldLeft(scheduleWithTaskRemoved) {
      (schedule: Schedule, assignment: (Int, Person)) =>
        changeWeek(schedule, assignment._1, task, assignment._2)
    }

  protected[domain] def calculateScoreMaxTasks(schedule: Schedule, maxTasks: Int): Int =
    schedule.weekList.foldLeft(0)((x, y) => x + Week.calculateScoreMaxTasks(y, maxTasks))

  protected[domain] def calculateScore(schedule: Schedule, personList: List[Person]): Int =
    schedule.weekList.foldLeft(0)((x, y) => x + Week.calculateScore(y, personList))

  protected[domain] def calculateScoreBalanced(schedule: Schedule, personList: List[Person]): Int =
    val regularScore = schedule.weekList.foldLeft(0)((x, y) => x + Week.calculateScore(y, personList))
    val workLoads = personList.map { person =>
      schedule.weekList.map { week =>
        week.taskAssignment.map { (task, assignedPerson) =>
          if person == assignedPerson then task.costs else 0
        }.sum
      }.sum
    }
    val mean = workLoads.sum.toDouble / workLoads.size
    val variance = workLoads.map(x => math.pow(x - mean, 2)).sum / workLoads.size
    val standardDev = math.sqrt(variance).toInt
    regularScore - standardDev


  protected[domain] def defaultSchedule(taskList: List[Task],
                                        startWeek: Int,
                                        scheduleLength: Int,
                                        createdAt: DateTime
                                       ): Schedule = {
    @tailrec
    def addAllTasksToWeekList(taskList: List[Task],
                              weekMap: TreeMap[Int, Week],
                              startWeek: Int,
                              scheduleLength: Int,
                             ): List[Week] =
      taskList match
        case Nil => weekMap.values.toList
        case task :: rest =>
          val updatedWeekMap = updateRelevantWeeks(task, weekMap, startWeek, scheduleLength)
          addAllTasksToWeekList(rest, updatedWeekMap, startWeek, scheduleLength)

    def updateRelevantWeeks(task: Task,
                            weekMap: TreeMap[Int, Week],
                            startWeek: Int,
                            scheduleLength: Int,
                           ): TreeMap[Int, Week] =
      val relevantWeekNumbers = relevantWeeks(task, startWeek, scheduleLength)
      weekMap.map((weekNumber, week) =>
        if relevantWeekNumbers.contains(weekNumber) then
          val updatedWeek = weekMap(weekNumber).copy(taskAssignment =
            weekMap(weekNumber).taskAssignment + (task -> task.preferredAssignee))
          (weekNumber, updatedWeek)
        else
          (weekNumber, week)
      )

    val weekMap = (startWeek until startWeek + scheduleLength)
      .map(weekNumber => (weekNumber, Week(weekNumber, Map[Task, Person]())))
      .to(TreeMap)
    val weekList = addAllTasksToWeekList(taskList, weekMap, startWeek, scheduleLength)

    Schedule(createdAt, weekList)
  }

  protected[domain] def relevantWeeks(task: Task, startWeek: Int, scheduleLength: Int): Seq[Int] =
    task match {
      case task if task.isRepeated =>
        val start = if (task.lastDoneAt + task.minRepeatInterval <= startWeek) ||
          (startWeek + scheduleLength < task.lastDoneAt + task.minRepeatInterval)
        then startWeek else task.lastDoneAt + task.minRepeatInterval
        start until startWeek + scheduleLength by task.minRepeatInterval
      case _ =>
        val start = if startWeek + scheduleLength < startWeek + task.maxRepeatInterval
        then startWeek else Math.max(startWeek, startWeek + task.maxRepeatInterval - 1)
        Seq(start)
    }
