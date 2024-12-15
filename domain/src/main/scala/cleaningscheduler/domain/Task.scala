package cleaningscheduler.domain

import scala.util.{Failure, Success, Try}

case class Task(name: String,
                costs: Int,
                preferredAssignee: Person,
                isPreferredFixed: Boolean,
                lastDoneAt: Int,
                isRepeated: Boolean,
                minRepeatInterval: Int,
                maxRepeatInterval: Int
               ) extends ITask {
  require(minRepeatInterval <= maxRepeatInterval,
    "minRepeatInterval should be smaller or equal to maxRepeatInterval")
  require(costs >= 0)
  require(lastDoneAt > 0 && lastDoneAt <= 52)
}

object Task:
  def changeName(task: Task, newName: String): Task = task.copy(name = newName)

  def changeCosts(task: Task, newCosts: Int): Task = task.copy(costs = newCosts)

  def changePreferredAssignee(task: Task, person: Person): Task = task.copy(preferredAssignee = person)

  def changeIsPreferredFixed(task: Task, bool: Boolean): Task = task.copy(isPreferredFixed = bool)

  def changeLastDoneAt(task: Task, newLastDoneAt: Int): Task = task.copy(lastDoneAt = newLastDoneAt)

  def changeIsRepeated(task: Task, isRepeatedNew: Boolean): Task = task.copy(isRepeated = isRepeatedNew)

  def changeMinRepeatInterval(task: Task, newMinRepeatInterval: Int): Try[Task] =
    if newMinRepeatInterval <= task.maxRepeatInterval then
      Success(task.copy(minRepeatInterval = newMinRepeatInterval))
    else
      Failure(IllegalArgumentException("Cannot change minRepeatInterval to higher value than maxRepeatInterval"))

  def changeMaxRepeatInterval(task: Task, newMaxRepeatInterval: Int): Try[Task] =
    if newMaxRepeatInterval >= task.maxRepeatInterval then
      Success(task.copy(maxRepeatInterval = newMaxRepeatInterval))
    else
      Failure(java.lang.IllegalArgumentException("Cannot change maxRepeatInterval to lower value than minRepeatInterval"))