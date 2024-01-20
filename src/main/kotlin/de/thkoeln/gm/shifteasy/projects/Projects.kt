package de.thkoeln.gm.shifteasy.projects

import jakarta.persistence.*
import java.util.*
import javax.annotation.processing.Generated
import org.hiberate.annotations.GenericGenerator
import kotlin.collections.ArrayList

@Entity
@Table(name="APP_PROJECTS")
class Projects{
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name="uuid2", strategy = "uuid2")
    var id: UUID = java.util.UUID.randomUUID()
    var estimated_hours: Int = 0
    var budget: Double = 0.0
    var start_date: String = ""
    var end_date: String = ""
    var status: String = "created"

    override fun toString(): String {
        return "Id: $id, Estimated_Hours: $estimated_hours, Budget: $budget, Start_Date: $start_date, End_Date: $end_date, Status: $status"
    }
}