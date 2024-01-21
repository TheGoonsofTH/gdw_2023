package de.thkoeln.gm.shifteasy.projects

//import org.springframework.stereotype.Controller
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*
import java.util.Date
import java.util.UUID
data class createProjectDTO(val  estimated_hours: Int, val budget: Double, val start_date: Instant, val end_date: Instant, val status: String?)

@RestController
class ProjectsController(private val projectsService: ProjectsService) {

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    fun saveProjects(@RequestBody body: createProjectDTO) : Projects {
        val projects = Projects()
        projects.estimated_hours = body.estimated_hours
        projects.budget = body.budget
        projects.start_date = body.start_date
        projects.end_date = body.end_date
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
    fun updateProjects(@PathVariable id: UUID, @RequestBody body: createProjectDTO): Projects? {
        var projects = projectsService.findById(id)?:throw ResponseStatusException(HttpStatus.NOT_FOUND)
        projects.estimated_hours = body.estimated_hours
        projects.budget = body.budget
        projects.start_date = body.start_date
        projects.end_date = body.end_date
        if (body.status != null) {
            projects.status = body.status
        }
        projectsService.save(projects)
        return projects
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProjects(id: UUID) {
        var projects: Projects? = projectsService.findById(id)
        if (projects != null) {
            projectsService.delete(projects)
        }
    }

    @DeleteMapping("/projects")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAllProjects(){
        var allProjects: List<Projects> = projectsService.findAll()
        for (projects in allProjects) {
            projectsService.delete(projects)
        }
    }
}