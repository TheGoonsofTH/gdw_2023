package de.thkoeln.gm.shifteasy.generation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
data class Job(
    val jobTitle: String,
    val multiplier: Double
)

data class Project(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val estimatedHours: Int,
    val budget: Double,
    val startDate: Instant,
    val status: String // Assuming that "status" can have values other than just "created" or "running"
)

@Serializable
data class Freelancer(
    val id: String,
    val lohnStunde: Int,
    val stundenMonat: Int,
    val name: String,
    val job: Job
)

data class Festangestellter(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val lohnMonat: Int,
    val stundenMonat: Int,
    val name: String,
    val job: Job
)

data class Distribution(
    @Serializable(with = UUIDSerializer::class)
    val projektId: UUID,
    var usedBudget: Double,
    val estimatedEndDate: Instant,
    val startDate: Instant,
    val festangestellte: List<Festangestellter>,
    val freelancer: MutableList<Freelancer>
)
val jsonDecoder = Json {ignoreUnknownKeys = true}
fun getFeiertageURL(startDate: String, endDate: String) =
    "https://openholidaysapi.org/PublicHolidays?countryIsoCode=DE&languageIsoCode=DE&validFrom=$startDate&validTo=$endDate"

fun getWorkValue(free: Freelancer): Double {
    return free.job.multiplier * free.lohnStunde
}


fun fillArrayTill(
    sortedFestangestellte: List<Festangestellter>,
    remainingBudget: Double,
    acc: MutableList<Festangestellter>
): Pair<Double, List<Festangestellter>> {
    if (sortedFestangestellte.isEmpty()) return Pair(remainingBudget, acc)

    val (expensiver, rest) = sortedFestangestellte.first() to sortedFestangestellte.drop(1)

    if(remainingBudget < expensiver.lohnMonat){
        return fillArrayTill(rest,remainingBudget,acc)
    }

    if (remainingBudget <= 0 ) {
        return Pair(remainingBudget, acc)
    }

    acc.add(expensiver)
    return fillArrayTill(rest, remainingBudget - expensiver.lohnMonat, acc)
}

fun getEstimatedEndDate(
    monthlyHours: Int,
    startDate: Instant,
    estimatedHours: Long
): Instant {
    if(monthlyHours <= 0) return startDate;
    val totalMonths = (estimatedHours / monthlyHours)

    // Convert Instant to LocalDate
    val localStartDate = startDate.atZone(ZoneId.systemDefault()).toLocalDate()

    // Calculate the end date by adding the total months to the start date
    val localEndDate = localStartDate.plusMonths(totalMonths).plus(estimatedHours % monthlyHours,ChronoUnit.HOURS)

    // Convert LocalDate back to Instant
    val endDate = localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()

    return endDate
}

@OptIn(ExperimentalSerializationApi::class)
fun fetchPublicHolidays(startDate: Instant, endDate: Instant): List<PublicHoliday> {
    val dateFmt = DateTimeFormatter
        .ofPattern("yyyy-MM-dd")
        .withLocale( Locale.GERMANY )
        .withZone( ZoneId.systemDefault() );
    val startDataFmt = dateFmt.format(startDate)
    val endDataFmt = dateFmt.format(endDate)
    val url = getFeiertageURL(startDataFmt, endDataFmt)
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    return if (response.statusCode() == 200) {
        val json = response.body()
       try{
           return jsonDecoder.decodeFromString<List<PublicHoliday>>(json)
       }catch(e : Exception){
           println(e)
           throw ResponseStatusException(HttpStatus.BAD_GATEWAY,"Invalid Response from Holiday API")
       }
    } else {
        throw ResponseStatusException(HttpStatus.BAD_GATEWAY,"Invalid Status from Holiday API $url "+response.statusCode())

    }
}

fun balance(
    project: Project,
    festangestellte: List<Festangestellter>,
    freelancer: List<Freelancer>,
    targetDate: Instant
): Distribution {
    var remainingBudget = project.budget
    val sortedFestangestellte = festangestellte.sortedByDescending { it.lohnMonat }
    val filledUpFest = fillArrayTill(sortedFestangestellte, remainingBudget, mutableListOf())
    var monthlyHours = filledUpFest.second.sumOf { it.stundenMonat }+1
    val monthlyCost = filledUpFest.second.sumOf { it.lohnMonat }+1
    val sortedFreelancer = freelancer.sortedBy { getWorkValue(it) }
    val estimatedEndDateFull = getEstimatedEndDate(monthlyHours, project.startDate, project.estimatedHours.toLong())
    val holidays = fetchPublicHolidays(project.startDate, estimatedEndDateFull).stream().count()
    val estimatedEndDate = estimatedEndDateFull.plus(holidays, ChronoUnit.DAYS)
    val tillTime = Duration.between(project.startDate, estimatedEndDate).toHours()
    val result = Distribution(
        projektId = project.id,
        usedBudget = monthlyCost.toDouble(),
        estimatedEndDate = estimatedEndDate,
        startDate = project.startDate,
        festangestellte = filledUpFest.second,
        freelancer = mutableListOf()
    )

    if (tillTime <= 0) {
        val festCost = tillTime * monthlyCost
        result.usedBudget += festCost.toInt()
        return result
    }

    val targetDateMonths = Duration.between(project.startDate, targetDate).toDays() / 30 + 1
    val minFestCost = monthlyCost * targetDateMonths
    val minFestHours = monthlyHours * targetDateMonths
    remainingBudget -= minFestCost

    if (remainingBudget > 0) {
        var newestimatedEndDate = getEstimatedEndDate(
            monthlyHours,
            project.startDate,
            project.estimatedHours - minFestHours
        )
        var newTillTime = Duration.between(project.startDate, newestimatedEndDate).toHours().coerceAtLeast(0)

        while (remainingBudget > 0 && newTillTime > 0) {
            val free = sortedFreelancer.firstOrNull()
                ?:throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough freelancer for target date and budget "
                        + ("target_date" to targetDate)
                        + ("budget" to project.budget))

            val freeCost = free.lohnStunde * free.stundenMonat * targetDateMonths.toDouble()
            val reducedBudget = remainingBudget-freeCost;
            if (reducedBudget < 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST,"Not enough budget for freelancer"
                    + ("freelancer" to freeCost)
                    + ("budget" to project.budget)
                    + ("reducedBudget" to reducedBudget))

            result.freelancer.add(free)
            remainingBudget -= freeCost.toInt()
            monthlyHours += free.stundenMonat * targetDateMonths.toInt()
            newestimatedEndDate = getEstimatedEndDate(
                monthlyHours,
                project.startDate,
                project.estimatedHours - minFestHours
            )
            newTillTime = Duration.between(project.startDate, newestimatedEndDate).toHours().coerceAtLeast(0)
        }

        result.usedBudget = project.budget - remainingBudget
        return result
    }

    throw ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Cannot meet target date with budget"
                + ("target_date" to targetDate)
                + ("budget" to project.budget)
    )
}