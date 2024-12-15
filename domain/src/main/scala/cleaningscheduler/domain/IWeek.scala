package cleaningscheduler.domain

trait IWeek {
  def weekNumber: Int

  def getTaskAssignment: Map[ITask, IPerson]
  
  def getTaskAssignmentAsJava: java.util.Map[ITask, IPerson]
}
