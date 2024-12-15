package cleaningscheduler.domain

import com.github.nscala_time.time.Imports.DateTime

trait ISchedule {
  def weekList: List[IWeek]
  
  def weekListAsJava: java.util.List[IWeek]
  
  def createdAt: DateTime
}
