package cleaningscheduler.domain.search

import cleaningscheduler.domain.{Person, Schedule, Task}

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

object ScheduleSearcherOnlyPersonVariable:
  private type ScheduleOption = (Task, Int, List[Person])
  private type OptionTreeNodeValue = (Task, Int, Person)

  protected[domain] def getOptionsList(taskList: List[Task],
                                       personList: List[Person],
                                       startWeek: Int,
                                       scheduleLength: Int): List[ScheduleOption] =
    val listBuffer = new ListBuffer[(Task, Int, List[Person])]
    taskList.foreach(task =>
      val relevantWeeks = Schedule.relevantWeeks(task, startWeek, scheduleLength)
      val personListForTask = if (task.isPreferredFixed) List(task.preferredAssignee) else personList
      relevantWeeks.foreach(weekNumber => listBuffer.addOne(task, weekNumber, personListForTask))
    )
    listBuffer.toList


  protected[domain] def buildOptionTree(optionsList: List[ScheduleOption]): Tree[OptionTreeNodeValue] =
    def buildTreeHelper(remaining: List[ScheduleOption]): List[Tree[OptionTreeNodeValue]] =
      remaining match
        case Nil => List()
        case x :: xs =>
          x._3.map(person => Node((x._1, x._2, person), buildTreeHelper(xs)))

    Node(null, buildTreeHelper(optionsList))

  protected[domain] def buildScheduleTree(optionsTree: Tree[OptionTreeNodeValue], schedule: Schedule): Tree[Schedule] =
    def buildScheduleTreeHelper(remaining: Tree[OptionTreeNodeValue],
                                currentSchedule: Schedule
                               ): List[Tree[Schedule]] =
      remaining match
        case Node(null, children) => children.map(childNode =>
          Node(currentSchedule, buildScheduleTreeHelper(childNode, currentSchedule))
        )
        case Node((task, weekNumber, person), List()) =>
          val updatedSchedule = Schedule.changeWeek(currentSchedule, weekNumber, task, person)
          List(Node(updatedSchedule, List()))
        case Node((task, weekNumber, person), children) =>
          val updatedSchedule = Schedule.changeWeek(currentSchedule, weekNumber, task, person)
          children.map(childNode =>
            Node(updatedSchedule, buildScheduleTreeHelper(childNode, updatedSchedule))
          )

    Node(schedule, buildScheduleTreeHelper(optionsTree, schedule))