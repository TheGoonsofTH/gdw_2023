package de.thkoeln.gm.shifteasy.employee

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import java.util.UUID
data class createEmplyeeDTO(val lohn: Int, val stunden: Int, val name: String, val job: String)

@RestController
class EmployeeController(private val employeeService: EmployeeService) {

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    fun saveEmployee(@RequestBody body: createEmplyeeDTO) : Employee {
        val employee = Employee()
        employee.lohn = body.lohn
        employee.stunden = body.stunden
        employee.name = body.name
        employee.job = body.job
        employeeService.save(employee)

        return employee
    }

    @GetMapping("/employees")
    fun getAllEmployee(): List<Employee> {
        return employeeService.findAll()
    }

    @GetMapping("/employees/{id}")
    fun getEmployeeByID(@PathVariable id: UUID): Employee {
        val employee: Employee? = employeeService.findById(id)
        if(employee!= null) {
            return employee
        }else{
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @PutMapping("/employees/{id}")
    fun updateEmployeeByID(@PathVariable id:UUID, lohn: Int, stunden: Int, name: String, job: String){
        var employee: Employee? = employeeService.findById(id)
        if(employee!= null){
            if(lohn!=null) employee.lohn = lohn
            if(stunden!=null) employee.stunden = stunden
            if(name!=null) employee.name = name
            if(job!=null) employee.job = job

        }else{
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteEmployee(@PathVariable id: UUID) {
        val employee: Employee? = employeeService.findById(id)
        if(employee!=null){
            employeeService.delete(employee)
        }else{
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }
}