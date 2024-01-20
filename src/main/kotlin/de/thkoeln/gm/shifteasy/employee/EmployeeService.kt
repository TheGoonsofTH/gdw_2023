package de.thkoeln.gm.shifteasy.employee

import java.util.Date
import java.util.UUID

interface EmployeeService{
    fun findById(id: UUID): Employee?
    fun findAll(): List<Employee>
    fun save(employee: Employee)
    fun delete(employee: Employee)
}