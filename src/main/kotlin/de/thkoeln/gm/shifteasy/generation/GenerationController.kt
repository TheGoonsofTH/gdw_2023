package de.thkoeln.gm.shifteasy.generation

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant



data class GenerationRequestSchema(
    val project: Project,
    val freelancer: List<Freelancer>,
    val festangestellte: List<Festangestellter>,
    val targetDate: Instant
)


@RestController
class GenerationController {

    @PostMapping("/generate")
    fun getAllProjects(@RequestBody body: GenerationRequestSchema): Distribution {
        return balance(body.project, body.festangestellte, body.freelancer,body.targetDate)
    }

}