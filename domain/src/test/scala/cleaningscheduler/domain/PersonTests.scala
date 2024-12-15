package cleaningscheduler.domain

import org.scalatest.wordspec.AnyWordSpec

class PersonTests extends AnyWordSpec {
  "Person" when {
    "constructed" should {
      "have name" in {
        val person = Person("PersonA", Map[Int, Int]())
        assert(person.name == "PersonA")
      }
      "have availabilityAssignment" in {
        val person = Person("PersonA", Map[Int, Int]())
        assert(person.availabilityAssignment == Map[Int, Int]())
      }
      "have availabilityAssignment as java Map" in {
        val person = Person("PersonA", Map[Int, Int]())
        val aaAsJava = person.getAvailabilityAssignmentAsJavaMap 
        assert(aaAsJava.isInstanceOf[java.util.Map[Integer, Integer]])
      }
    }
    "changed" should {
      "create new person with new name" in {
        val person = Person("PersonA", Map[Int, Int]())
        val newPerson = Person.changeName(person, "PersonB")
        assert(newPerson.name == "PersonB")
      }
      "create new person with updated availability assignment" in {
        val person = Person("PersonA", Map[Int, Int]())
        val newAvailability = Map(30 -> 30, 31 -> 60, 32 -> 45)
        val updatedPerson = Person.changeAvailability(person, newAvailability)
        assert(updatedPerson.availabilityAssignment == newAvailability)
      }
    }
  }


}
