package de.thkoeln.gm.shifteasy.projects

import de.thkoeln.gm.shifteasy.employee.Employee
import java.time.Instant
import java.util.Date
import java.util.UUID

interface ProjectsService{
    fun findById(id: UUID): Projects?
    fun findAll(): List<Projects>
    fun save(projects: Projects)
    fun delete(projects: Projects)

    fun findInTimeframe(startDate: Instant, endDate: Instant) : List<Employee>
}