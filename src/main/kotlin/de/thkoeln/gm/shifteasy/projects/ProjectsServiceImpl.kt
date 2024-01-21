package de.thkoeln.gm.shifteasy.projects

import de.thkoeln.gm.shifteasy.employee.Employee
import de.thkoeln.gm.shifteasy.employee.EmployeeRepository
import de.thkoeln.gm.shifteasy.employee.EmployeeService
import de.thkoeln.gm.shifteasy.employee.EmployeeServiceImpl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID


@Service
class ProjectsServiceImpl (private val projectsRepository: ProjectsRepository,private val employeeService: EmployeeService) : ProjectsService {
    override fun findById(id: UUID): Projects? {
        return projectsRepository.findByIdOrNull(id)
    }

    override fun findAll(): List<Projects> {
        return projectsRepository.findAll().toList();
    }

    override fun save(projects: Projects) {
        projectsRepository.save(projects)
    }

    override fun delete(projects: Projects) {
        projectsRepository.delete(projects)
    }

    override fun findInTimeframe(start_date: Instant, end_date: Instant): List<Employee> {
        val projects = projectsRepository.findByStart_dateGreaterThanEqualAndEnd_dateLessThanEqual(start_date, end_date)
        val employees = projects.stream().flatMap { it.employee.stream() }.map { it.id }
        val allEmployee = employeeService.findAll()
        return allEmployee.stream().filter { employees.inc}
    }

}

fun filterListAById(listA: List<Employee>, listB: List<Employee>): List<Employee> {
    val idsToRemove = listB.map { it.id }.toSet()
    return listA.filterNot { idsToRemove.contains(it.id) }
}