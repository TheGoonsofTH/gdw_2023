package de.thkoeln.gm.shifteasy.projects

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface ProjectsRepository : CrudRepository<Projects, UUID> {

}