package cleaningscheduler.domain.search

import cleaningscheduler.domain.{Person, Schedule, Task}

import scala.util.chaining.scalaUtilChainingOps
import scala.language.postfixOps

object ScheduleSearcher:
  private type ScheduleOption = (Task, List[List[(Int, Person)]])
  private type OptionTreeNodeValue = (Task, List[(Int, Person)])

  protected[domain] def listOfRelevantWeekSequences(task: Task,
                                                    startWeek: Int,
                                                    scheduleLength: Int,
                                                    isIntervalVariable: Boolean = true
                                                   ): Seq[Seq[Int]] =
    def generateSequences(currentWeek: Int, sequence: Seq[Int]): Seq[Seq[Int]] =
      val sequences = Seq(sequence)

      val repeatRange = if isIntervalVariable
      then task.minRepeatInterval to task.maxRepeatInterval else task.minRepeatInterval to task.minRepeatInterval

      val nextSteps = repeatRange
        .map(currentWeek + _)
        .filter(weekNumber => weekNumber < startWeek + scheduleLength + task.minRepeatInterval)

      if nextSteps.isEmpty then sequences
      else nextSteps.flatMap(next => generateSequences(next, sequence :+ next))

    if (task.isRepeated && task.lastDoneAt + task.maxRepeatInterval < startWeek
      || task.lastDoneAt + task.minRepeatInterval > startWeek + scheduleLength) {
      generateSequences(startWeek, Seq(startWeek))
        .map(sequence => sequence.filter(weekNumber => weekNumber < startWeek + scheduleLength))
        .distinct
    } else if (task.isRepeated) {
      val possibleStarts = ((task.lastDoneAt + task.minRepeatInterval) to (task.lastDoneAt + task.maxRepeatInterval))
        .filter(weekNumber => weekNumber >= startWeek && weekNumber < startWeek + scheduleLength)
        .distinct
      possibleStarts.flatMap(start => generateSequences(start, Seq(start))
        .map(sequence => sequence.filter(weekNumber => weekNumber < startWeek + scheduleLength))
        .distinct
      )
    } else {
      (startWeek to startWeek + task.maxRepeatInterval)
        .filter(week => week < startWeek + scheduleLength)
        .map(week => Seq(week))
    }

  private def pickSample[A](list: List[A], sampleSize: Int): List[A] =
    if (list.length > sampleSize)
      val randomIndices = scala.util.Random.shuffle(list.indices).take(sampleSize)
      randomIndices.map(list).toList
    else
      list

  protected[domain] def getOptionsList(taskList: List[Task],
                                       personList: List[Person],
                                       startWeek: Int,
                                       scheduleLength: Int,
                                       quickVersion: Boolean = true,
                                       isIntervalVariable: Boolean = true
                                      ): List[ScheduleOption] =
    taskList.map { task =>
      (task,
        listOfRelevantWeekSequences(task, startWeek, scheduleLength, isIntervalVariable).toList
          .flatMap { weekList =>
            val validPersons =
              if task.isPreferredFixed then List(task.preferredAssignee)
              else if !isIntervalVariable && !quickVersion then personList
              else if quickVersion then pickSample(personList, 1)
              else personList
            val combinations = weekList.map { week =>
              validPersons.map(person => (week, person))
            }

            combinations.foldLeft(List(List.empty[(Int, Person)])) {
              (acc, weekCombinations) =>
                for {
                  seq <- acc
                  combo <- weekCombinations
                } yield seq :+ combo
            }
          }
          .pipe(list => if quickVersion then pickSample[List[(Int, Person)]](list, 50) else list)
      )
    }

  protected[domain] def getAllCombinedOptions(optionsList: List[ScheduleOption]): List[List[OptionTreeNodeValue]] =
    def cartesianProduct[A, B](listA: List[A], listB: List[B]): List[(A, B)] = {
      for {
        a <- listA
        b <- listB
      } yield (a, b)
    }

    optionsList.foldLeft(List(List.empty[(Task, List[(Int, Person)])])) {
      (acc, tuple: ScheduleOption) =>
        val task = tuple._1
        val options = tuple._2
        val taskCombinations = options.map(option => (task, option))

        cartesianProduct(acc, taskCombinations)
          .map { (accOption, taskOption) =>
            accOption :+ taskOption
          }
    }

  protected[domain] def buildOptionTree(optionsList: List[ScheduleOption]): Tree[OptionTreeNodeValue] =
    def buildTreeHelper(remaining: List[ScheduleOption]): List[Tree[OptionTreeNodeValue]] =
      remaining match
        case Nil => List()
        case x :: xs =>
          x._2.map(weekList => Node((x._1, weekList), buildTreeHelper(xs)))

    Node(null, buildTreeHelper(optionsList))

  protected[domain] def buildScheduleTree(optionsTree: Tree[OptionTreeNodeValue], schedule: Schedule): Tree[Schedule] =
    def buildScheduleTreeHelper(remaining: Tree[OptionTreeNodeValue],
                                currentSchedule: Schedule
                               ): List[Tree[Schedule]] =
      remaining match
        case Node(null, children) => children.map(childNode =>
          Node(currentSchedule, buildScheduleTreeHelper(childNode, currentSchedule))
        )
        case Node((task, weekOption), List()) =>
          val updatedSchedule: Schedule = Schedule.reassignTask(currentSchedule, task, weekOption)
          List(Node(updatedSchedule, List()))
        case Node((task, weekOption), children) =>
          val updatedSchedule: Schedule = Schedule.reassignTask(currentSchedule, task, weekOption)
          children.map(childNode =>
            Node(updatedSchedule, buildScheduleTreeHelper(childNode, updatedSchedule))
          )

    Node(schedule, buildScheduleTreeHelper(optionsTree, schedule))

  protected[domain] def searchBestSchedule(searchTree: Tree[Schedule],
                                           costFunction: Schedule => Int
                                          ): (Schedule, Int) =
    def searchBestScheduleHelper(remainingTree: Tree[Schedule], acc: (Schedule, Int)): (Schedule, Int) =
      remainingTree match
        case Node(schedule, children) =>
          val score = costFunction(schedule)
          val newAcc = if (score > acc._2) (schedule, score) else acc
          children.foldLeft(newAcc) { (currentBest, child) =>
            val childBest = searchBestScheduleHelper(child, currentBest)
            if (childBest._2 > currentBest._2) childBest else currentBest
          }

    val best = searchBestScheduleHelper(searchTree, (null.asInstanceOf[Schedule], Int.MinValue))
    best