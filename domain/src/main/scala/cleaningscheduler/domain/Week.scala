package cleaningscheduler.domain

import java.util
import scala.jdk.CollectionConverters.*

case class Week(weekNumber: Int,
                protected[domain] val taskAssignment: Map[Task, Person]
               ) extends IWeek {

  override def getTaskAssignment: Map[ITask, IPerson] =
    this.taskAssignment.map { (task, person) => (task: ITask, person: IPerson) }

  override def getTaskAssignmentAsJava: util.Map[ITask, IPerson] =
    this.taskAssignment.map { (k, v) =>
      (k.asInstanceOf[ITask], v.asInstanceOf[IPerson])
    }.asJava
}

object Week:
  def changeTaskAssignment(week: Week, newTaskAssignment: Map[Task, Person]): Week =
    week.copy(taskAssignment = newTaskAssignment)

  protected[domain] def calculateScoreMaxTasks(week: Week, maxTasks:Int): Int =
    val numberOfTasks = week.taskAssignment.keys.size
    if (numberOfTasks < maxTasks) maxTasks - numberOfTasks else (-Math.pow(maxTasks - numberOfTasks, 2)).toInt

  protected[domain] def calculateScore(week: Week, personList: List[Person]): Int =
    personList.map(person =>
      val availability = person.availabilityAssignment.getOrElse(week.weekNumber, 0)
      val tasksForPerson = week.taskAssignment.filter(_._2 == person).keys.toList
      val costs = tasksForPerson.map(task => task.costs).sum
      if availability >= costs then availability - costs
        else (-Math.pow(availability - costs, 2)).toInt).sum