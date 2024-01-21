package de.thkoeln.gm.shifteasy.projects

import de.thkoeln.gm.shifteasy.employee.Employee
import de.thkoeln.gm.shifteasy.employee.EmployeeService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*


@Service
class ProjectsServiceImpl(
    private val projectsRepository: ProjectsRepository,
    private val employeeService: EmployeeService
) : ProjectsService {
    override fun findById(id: UUID): Projects? {
        return projectsRepository.findByIdOrNull(id)
    }

    override fun findAll(): List<Projects> {
        return projectsRepository.findAll().toList()
    }

    override fun save(projects: Projects) {
        projectsRepository.save(projects)
    }

    override fun delete(projects: Projects) {
        projectsRepository.delete(projects)
    }

    override fun findInTimeframe(start_date: Instant, end_date: Instant): List<Employee> {
        val projects = projectsRepository
            .findAll()
            .toList()
            .stream()
            .filter{it.start_date.isAfter(start_date)
                    && it.end_date.isBefore(end_date)
                    && it.status.equals("started")}
        projectsRepository.findAll()
        val employees = projects
            //.stream()
            .flatMap { it.employee.stream() }
            .map { it.id }
            .toList()
            .toSet()
        val allEmployee = employeeService.findAll()
        return allEmployee.toSet().filterNot { employees.contains(it.id) }
    }

}

