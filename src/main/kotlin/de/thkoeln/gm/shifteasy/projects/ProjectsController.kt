package de.thkoeln.gm.shifteasy.projects

//import org.springframework.stereotype.Controller
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import java.util.Date
import java.util.UUID

@RestController
class ProjectsController(private val projectsService: ProjectsService) {

    @PostMapping("/projects/{estimated_hours}/{budget}/{start_date}/{end_date}")
    @ResponseStatus(HttpStatus.CREATED)
    fun saveProjects(@PathVariable estimated_hours: Int, @PathVariable budget: Double, @PathVariable start_date: String, @PathVariable end_date: String) : Projects {
        val projects = Projects()
        projects.estimated_hours = estimated_hours
        projects.budget = budget
        projects.start_date = start_date
        projects.end_date = end_date
        projectsService.save(projects)

        return projects
    }

    @GetMapping("/projects")
    fun getAllProjects(): List<Projects> {
        return projectsService.findAll()
    }

    @GetMapping("/projects/{id}")
    fun getProjects(id: UUID): Projects {
        val projects: Projects? = projectsService.findById(id)
        if(projects != null){
            return projects
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

    }

    @PutMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateProjects(id: UUID, estimated_hours: Int, budget: Double, start_date: String, end_date: String, status: String) {
        var projects: Projects? = projectsService.findById(id)
        if (projects != null) {
            if (estimated_hours != null) {
                projects.estimated_hours = estimated_hours
            }

            if (budget != null) {
                projects.budget = budget
            }

            if (start_date != null) {
                projects.start_date = start_date
            }

            if (end_date != null) {
                projects.end_date = end_date
            }

            if (status != null) {
                projects.status = status
            }
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProjects(id: UUID) {
        var projects: Projects? = projectsService.findById(id)
        if (projects != null) {
            projectsService.delete(projects)
        }
    }
}