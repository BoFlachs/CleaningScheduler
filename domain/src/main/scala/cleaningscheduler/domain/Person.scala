package cleaningscheduler.domain

import java.util
import scala.jdk.CollectionConverters.*

case class Person(name: String,
                  availabilityAssignment: Map[Int, Int]
                 ) extends IPerson {
  override def getAvailabilityAssignmentAsJavaMap: util.Map[Integer, Integer] =
    this.availabilityAssignment.map { (k, v) =>
      (k.asInstanceOf[java.lang.Integer], v.asInstanceOf[java.lang.Integer])
    }.asJava
}

object Person:
  def changeName(person: Person, newName: String): Person = person.copy(name = newName)

  def changeAvailability(person: Person, newAvailability: Map[Int, Int]): Person =
    person.copy(availabilityAssignment = newAvailability)