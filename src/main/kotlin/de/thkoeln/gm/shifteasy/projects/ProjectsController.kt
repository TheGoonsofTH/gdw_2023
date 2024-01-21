package de.thkoeln.gm.shifteasy.projects

//import org.springframework.stereotype.Controller
import de.thkoeln.gm.shifteasy.employee.Employee
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*
import java.util.Date
import java.util.UUID
data class createProjectDTO(val  estimated_hours: Int, val budget: Double, val start_date: Instant, val end_date: Instant, val status: String?)
data class startProjectDTO(val employee: List<Employee>, val usedBudget: Double)

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
    fun getProjects(@PathVariable id: UUID): Projects {
        var projects = projectsService.findById(id)?:throw ResponseStatusException(HttpStatus.NOT_FOUND)
            return projects
    }

    @PutMapping("/projects/{id}")
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
    fun deleteProjects(@PathVariable id: UUID) {
        var projects = projectsService.findById(id)?:throw ResponseStatusException(HttpStatus.NOT_FOUND)
            projectsService.delete(projects)
    }

    @DeleteMapping("/projects")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAllProjects(){
        var allProjects: List<Projects> = projectsService.findAll()
        for (projects in allProjects) {
            projectsService.delete(projects)
        }
    }
    @PostMapping("/projects/{id}")
    fun startProjects(@PathVariable id: UUID, @RequestBody body: startProjectDTO): Projects {
        var projects = projectsService.findById(id)?:throw ResponseStatusException(HttpStatus.NOT_FOUND)
        projects.status = "started"
        projects.employee = body.employee
        projects.usedBudget = body.usedBudget
        projectsService.save(projects)
        return projects
    }
}