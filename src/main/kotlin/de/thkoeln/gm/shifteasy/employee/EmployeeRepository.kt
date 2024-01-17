package de.thkoeln.gm.shifteasy.employee

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EmployeeRepository : CrudRepository<Employee, UUID> {

}