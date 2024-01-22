package de.thkoeln.gm.shifteasy.generation

import de.thkoeln.gm.shifteasy.projects.ProjectsService
import khttp.get
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

data class GenerationRequestSchema(
    val project: Project,
    val freelancer: List<Freelancer>,
    val festangestellte: List<Festangestellter>,
    val targetDate: Instant
)

data class GenerationRequestSchemaFull(
    val targetDate: Instant
)

data class Comment(
    val language: String,
    val text: String
)

data class Name(
    val language: String,
    val text: String
)

data class Subdivision(
    val code: String,
    val shortName: String
)

data class PublicHoliday(
    val comment: List<Comment>,
    val endDate: String,
    val id: String,
    val name: List<Name>,
    val nationwide: Boolean,
    val quality: String,
    val startDate: String,
    val subdivisions: List<Subdivision>,
    val type: String
)

@RestController
class GenerationController(private val projectsService: ProjectsService) {
    val freeLancerApi = "https://cool-sheep-35.deno.dev/api/freelancer"


    @PostMapping("/generate")
    fun genreate(@RequestBody body: GenerationRequestSchema): Distribution {
        return balance(body.project, body.festangestellte, body.freelancer, body.targetDate)
    }

    @PostMapping("/generate/{id}")
    fun generateFull(@PathVariable id: UUID, @RequestBody body: GenerationRequestSchemaFull): Distribution {
        val freelancer = fetchFreelancers()
        val projects = projectsService.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val festangestellte: List<Festangestellter> = projectsService.findInTimeframe(projects.start_date,body.targetDate)
            .map { Festangestellter(it.id,it.lohn,it.stunden,it.name,Job(it.job.jobTitle,it.job.multiplier)) }
        val project = Project(id,projects.estimated_hours, budget = projects.estimated_hours, startDate = projects.start_date as Instant, status = projects.status)
        return balance(project, festangestellte, freelancer, body.targetDate)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun fetchFreelancers(): List<Freelancer> {
        val response = get(freeLancerApi)

        return if (response.statusCode == 200) {
            val json = response.text
            Json.decodeFromString<List<Freelancer>>(json)
        } else {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY)
        }
    }

}


