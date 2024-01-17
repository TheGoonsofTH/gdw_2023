package de.thkoeln.gm.shifteasy.employee

import org.springframework.stereotype.Controller
import jakara.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import java.util.Date
import java.util.UUID

@Controller
class EmployeeController(private val employeeService: EmployeeService) {

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    fun saveEmployee(lohn: Int, stunden: Int, name: String, job: String, response: HttpServletResponse) : Employee {
        val employee = Employee()
        employee.lohn = lohn
        employee.stunden = stunden
        employee.name = name
        employee.job = job
        employeeService.save(employee)

        return employee
    }

    @GetMapping("/employees/{id}")
    fun getEmployee(@PathVariable id: UUID): Employee {
        val employee: Employee? = employeeService.findById(id)
        if(employee!= null) {
            return employee
        }else{
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/employees?start_date={date}&end_date={date}")
    fun TODO(): Nothing
    //fun getAllEmployee(startDate: Date, endDate: Date){

    }
}