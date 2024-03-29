package de.thkoeln.gm.shifteasy.employee

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

data class CreateJobDTO(val title:String, val multiplier:Double)
data class CreateEmployeeDTO(val lohn: Int, val stunden: Int, val name: String, val job: CreateJobDTO)

@RestController
class EmployeeController(private val employeeService: EmployeeService) {

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    fun saveEmployee(@RequestBody body: CreateEmployeeDTO) : Employee {
        val employee = Employee()
        employee.lohn = body.lohn
        employee.stunden = body.stunden
        employee.name = body.name
        employee.job = Job(body.job.title,body.job.multiplier)
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
    fun updateEmployeeByID(@PathVariable id:UUID, @RequestBody body: CreateEmployeeDTO): Employee {
        val employee = employeeService.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        employee.lohn = body.lohn
        employee.stunden = body.stunden
        employee.name = body.name
        employee.job = Job(body.job.title,body.job.multiplier)
        employeeService.save(employee)
        return employee


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