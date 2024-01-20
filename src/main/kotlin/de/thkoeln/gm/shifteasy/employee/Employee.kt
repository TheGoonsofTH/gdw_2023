package de.thkoeln.gm.shifteasy.employee

import de.thkoeln.gm.shifteasy.AbstractEntity
import jakarta.persistence.*
import java.util.*
import org.hibernate.annotations.GenericGenerator

@Entity
class Employee : AbstractEntity() {

    var lohn: Int = 0
    var stunden: Int = 0
    var name: String = ""
    var job: String = ""

    override fun toString(): String {
        return "Id: $id, Lohn: $lohn, Stunden: $stunden, Name: $name, Job: $job"
    }
}