package de.thkoeln.gm.shifteasy.projects

import de.thkoeln.gm.shifteasy.AbstractEntity
import de.thkoeln.gm.shifteasy.employee.Employee
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import java.time.Instant


@Entity
class Projects : AbstractEntity() {

    var estimated_hours: Int = 0
    var budget: Double = 0.0
    var start_date: Instant = Instant.now()
    var end_date: Instant = Instant.now()
    var status: String = "created"
    @OneToMany
    val employee:List<Employee> = listOf()

    override fun toString(): String {
        return "Id: $id, Estimated_Hours: $estimated_hours, Budget: $budget, Start_Date: $start_date, End_Date: $end_date, Status: $status"
    }
}