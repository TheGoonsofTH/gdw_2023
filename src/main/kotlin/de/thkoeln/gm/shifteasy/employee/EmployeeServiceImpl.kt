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
        
    }
}