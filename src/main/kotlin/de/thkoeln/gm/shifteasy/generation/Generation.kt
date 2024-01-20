package de.thkoeln.gm.shifteasy.generation

import java.time.*
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

data class Job(
    val jobTitle: String,
    val multiplier: Int
)

data class Project(
    val id: String,
    val estimatedHours: Int,
    val budget: Int,
    val startDate: Instant,
    val status: String // Assuming that "status" can have values other than just "created" or "running"
)

data class Freelancer(
    val id: String,
    val lohnStunde: Int,
    val stundenMonat: Int,
    val name: String,
    val job: Job
)

data class Festangestellter(
    val id: String,
    val lohnMonat: Int,
    val stundenMonat: Int,
    val name: String,
    val job: Job
)

data class Distribution(
    val projektId: String,
    var usedBudget: Int,
    val estimatedEndDate: Instant,
    val startDate: Instant,
    val festangestellte: List<Festangestellter>,
    val freelancer: MutableList<Freelancer>
)

fun getWorkValue(free: Freelancer): Int {
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


    return endDate.plus(remainingHours,ChronoUnit.HOURS)
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
    val estimatedEndDate = getEstimatedEndDate(monthlyHours, project.startDate, project.estimatedHours.toLong())
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