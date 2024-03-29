package de.thkoeln.gm.shifteasy.generation

import de.thkoeln.gm.shifteasy.projects.ProjectsService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class GenerationRequestSchema(
    val project: Project,
    val freelancer: List<Freelancer>,
    val festangestellte: List<Festangestellter>,
    val targetDate: Instant
)

data class GenerationRequestSchemaFull(
    val targetDate: Instant
)

@Serializable
data class Name(
    val language: String,
    val text: String
)
@Serializable
data class Subdivision(
    val code: String,
    val shortName: String
)
@Serializable
data class PublicHoliday(
    val endDate: String,
    val id: String,
    val name: List<Name>,
    val nationwide: Boolean,
    val startDate: String,
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
        val project = Project(id,projects.estimated_hours, budget = projects.budget, startDate = projects.start_date as Instant, status = projects.status)
        return balance(project, festangestellte, freelancer, body.targetDate)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun fetchFreelancers(): List<Freelancer> {
        val client = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(freeLancerApi)).build()
        val response = client.send(request,HttpResponse.BodyHandlers.ofString())


        return if (response.statusCode() == 200) {
            val json = response.body()
            try {
            Json.decodeFromString<List<Freelancer>>(json)
            }catch(e : Exception){
                println(e)
                throw ResponseStatusException(HttpStatus.BAD_GATEWAY,"Invalid Response from Freelancer API")
            }
        } else {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY,"Invalid Status from Freelancer API "+response.statusCode())
        }
    }

}


