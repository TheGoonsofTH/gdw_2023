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
    val budget: Int,
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
    var usedBudget: Int,
    val estimatedEndDate: Instant,
    val startDate: Instant,
    val festangestellte: List<Festangestellter>,
    val freelancer: MutableList<Freelancer>
)

fun getFeiertageURL(startDate: String, endDate: String) =
    "https://openholidaysapi.org/PublicHolidays?countryIsoCode=DE&languageIsoCode=DE&validFrom=$startDate&validTo=$endDate"

fun getWorkValue(free: Freelancer): Double {
    return free.job.multiplier * free.lohnStunde
}


fun fillArrayTill(
    sortedFestangestellte: List<Festangestellter>,
    remainingBudget: Int,
    acc: MutableList<Festangestellter>
): Pair<Int, List<Festangestellter>> {
    if (sortedFestangestellte.isEmpty()) return Pair(remainingBudget, acc)

    val (expensiver, rest) = sortedFestangestellte.first() to sortedFestangestellte.drop(1)

    if (remainingBudget <= 0 || remainingBudget < expensiver.lohnMonat) {
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
    val totalMonths = (estimatedHours / monthlyHours).toInt()

    // Convert Instant to LocalDate
    val localStartDate = startDate.atZone(ZoneId.systemDefault()).toLocalDate()

    // Calculate the end date by adding the total months to the start date
    val localEndDate = localStartDate.plusMonths(totalMonths.toLong())

    // Convert LocalDate back to Instant
    val endDate = localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
    val remainingHours = estimatedHours % monthlyHours


    return endDate.plus(remainingHours, ChronoUnit.HOURS)
}

@OptIn(ExperimentalSerializationApi::class)
fun fetchPublicHolidays(startDate: Instant, endDate: Instant): List<PublicHoliday> {
    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
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
        Json.decodeFromString<List<PublicHoliday>>(json)
    } else {
        emptyList()
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
    var monthlyHours = filledUpFest.second.sumOf { it.stundenMonat }
    val monthlyCost = filledUpFest.second.sumOf { it.lohnMonat }
    val sortedFreelancer = freelancer.sortedByDescending { getWorkValue(it) }
    val estimatedEndDateFull = getEstimatedEndDate(monthlyHours, project.startDate, project.estimatedHours.toLong())
    val holidays = fetchPublicHolidays(project.startDate, estimatedEndDateFull).stream().count()
    val estimatedEndDate = estimatedEndDateFull.plus(holidays, ChronoUnit.DAYS)
    val tillTime = Duration.between(project.startDate, estimatedEndDate).toHours()
    val result = Distribution(
        projektId = project.id,
        usedBudget = monthlyCost,
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
    remainingBudget -= minFestCost.toInt()

    if (remainingBudget > 0) {
        var newestimatedEndDate = getEstimatedEndDate(
            monthlyHours,
            project.startDate,
            project.estimatedHours - minFestHours
        )
        var newTillTime = Duration.between(project.startDate, newestimatedEndDate).toHours().coerceAtLeast(0)

        while (remainingBudget > 0 && newTillTime > 0) {
            val free = sortedFreelancer.firstOrNull()
                ?: throw IllegalArgumentException(
                    "Not enough freelancer for target date and budget "
                            + ("target_date" to targetDate)
                            + ("budget" to project.budget)
                )

            val freeCost = free.lohnStunde * free.stundenMonat * targetDateMonths

            if (remainingBudget - freeCost < 0) break // TODO add option overdraw

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

    throw IllegalArgumentException(
        "Cannot meet target date with budget"
                + ("target_date" to targetDate)
                + ("budget" to project.budget)
    )
}