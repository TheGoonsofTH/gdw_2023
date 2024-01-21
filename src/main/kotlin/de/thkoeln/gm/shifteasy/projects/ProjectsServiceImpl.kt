package de.thkoeln.gm.shifteasy.projects

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class ProjectsServiceImpl (private val projectsRepository: ProjectsRepository) : ProjectsService {
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
}