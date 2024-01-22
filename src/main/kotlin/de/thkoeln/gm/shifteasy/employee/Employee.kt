package de.thkoeln.gm.shifteasy.employee

import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Embeddable
 class Job(
    val jobTitle: String,
    val multiplier: Double
)
@Entity
class Employee  {

    @Id
    var id: UUID = UUID.randomUUID()
    var lohn: Int = 0
    var stunden: Int = 0
    var name: String = ""
    @Embedded
    var job: Job = Job("basic",1.0)

    override fun toString(): String {
        return "Id: $id, Lohn: $lohn, Stunden: $stunden, Name: $name, Job: $job"
    }
}