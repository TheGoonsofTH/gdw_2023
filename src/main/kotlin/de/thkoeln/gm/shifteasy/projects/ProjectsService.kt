package de.thkoeln.gm.shifteasy.projects

import java.util.Date
import java.util.UUID

interface ProjectsService{
    fun findById(id: UUID): Projects?
    fun findAll(): List<Projects>
    fun save(projects: Projects)
    fun delete(projects: Projects)
}