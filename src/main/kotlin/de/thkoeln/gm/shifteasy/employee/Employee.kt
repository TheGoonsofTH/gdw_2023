package de.thkoeln.gm.shifteasy.employee

import jakarta.persistence.*
import java.util.*
import javax.annotation.processing.Generated
import org.hiberate.annotations.GenericGenerator
import kotlin.collections.ArrayList

@Entity
@Table(name="APP_EMPLOYEE")
class Employee{
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name="uuid2", strategy = "uuid2")
    var id: UUID = java.util.UUID.randomUUID()
    var lohn: Int = 0
    var stunden: Int = 0
    var name: String = ""
    var job: String = ""

    override fun toString(): String {
        return "Id: $id, Lohn: $lohn, Stunden: $stunden, Name: $name, Job: $job"
    }
}