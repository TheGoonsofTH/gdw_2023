package de.thkoeln.gm.shifteasy.employee

import de.thkoeln.gm.shifteasy.projects.Projects
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class EmployeeServiceImpl (private val employeeRepository: EmployeeRepository) : EmployeeService {
    override fun findById(id: UUID): Employee? {
        return employeeRepository.findByIdOrNull(id)
    }

    override fun findAll(): List<Employee>{
        return employeeRepository.findAll().toList()
    }

    override fun save(employee: Employee) {
        employeeRepository.save(employee)
    }

    override fun delete(employee: Employee) {
        employeeRepository.delete(employee)
    }

    init {
        var employee1 = Employee()
        var employee2 = Employee()
        var employee3 = Employee()
        employee1.lohn = 4000
        employee1.stunden = 40
        employee1.name = "Max Musterman"
        employee1.job = "Javascript Dev"
        employeeRepository.save(employee1)
        employee2.lohn = 4200
        employee2.stunden = 40
        employee2.name = "Hans Gerhard"
        employee2.job = "Typescript Dev"
        employeeRepository.save(employee2)
        employee3.lohn = 2000
        employee3.stunden = 20
        employee3.name = "Julius Maier"
        employee3.job = "GDscript Dev"
        employeeRepository.save(employee3)
    }
}