package de.thkoeln.gm.shifteasy.projects

import de.thkoeln.gm.shifteasy.AbstractEntity
import jakarta.persistence.Entity


@Entity
class Projects : AbstractEntity() {

    var estimated_hours: Int = 0
    var budget: Double = 0.0
    var start_date: String = ""
    var end_date: String = ""
    var status: String = "created"

    override fun toString(): String {
        return "Id: $id, Estimated_Hours: $estimated_hours, Budget: $budget, Start_Date: $start_date, End_Date: $end_date, Status: $status"
    }
}