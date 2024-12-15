package cleaningscheduler.domain

trait ITask {
  def name: String
  
  def costs: Int
  
  def preferredAssignee: IPerson
  
  def isPreferredFixed: Boolean
  
  def lastDoneAt: Int
  
  def isRepeated: Boolean
  
  def minRepeatInterval: Int
  
  def maxRepeatInterval: Int 
}
