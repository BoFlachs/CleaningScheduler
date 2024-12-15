package cleaningscheduler.domain

trait IPerson {
  def name: String

  def availabilityAssignment: Map[Int, Int]

  def getAvailabilityAssignmentAsJavaMap: java.util.Map[Integer, Integer]
}
