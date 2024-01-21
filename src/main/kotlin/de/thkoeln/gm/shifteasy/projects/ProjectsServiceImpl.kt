package de.thkoeln.gm.shifteasy.projects

import de.thkoeln.gm.shifteasy.employee.Employee
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID


@Service
class ProjectsServiceImpl (private val projectsRepository: ProjectsRepository) : ProjectsService {
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
    }

}