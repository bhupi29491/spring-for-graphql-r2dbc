package com.bhupi.springforgraphql.controller;

import com.bhupi.springforgraphql.model.AddEmployeeInput;
import com.bhupi.springforgraphql.model.Department;
import com.bhupi.springforgraphql.model.Employee;
import com.bhupi.springforgraphql.model.UpdateSalaryInput;
import com.bhupi.springforgraphql.repository.DepartmentRepository;
import com.bhupi.springforgraphql.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RestController
@Slf4j
public class GraphqlController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    private final HttpGraphQlClient httpGraphQlClient;

    public GraphqlController(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, HttpGraphQlClient httpGraphQlClient) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.httpGraphQlClient = httpGraphQlClient;
    }

    Function<AddEmployeeInput, Employee> mapping = aei -> {
        var employee = new Employee();
        employee.setName(aei.getName());
        employee.setSalary(aei.getSalary());
        employee.setDepartmentId(aei.getDepartmentId());
        return employee;
    };

    //    @SchemaMapping(typeName = "Mutation", field = "addEmployee")
    @MutationMapping
    public Mono<Employee> addEmployee(@Argument AddEmployeeInput addEmployeeInput) {
        return this.employeeRepository.save(mapping.apply(addEmployeeInput));
    }

    @QueryMapping
    public Flux<Employee> employeeByName(@Argument String employeeName) {
        return this.employeeRepository.getEmployeeByName(employeeName);
    }

    @MutationMapping
    public Mono<Employee> updateSalary(@Argument UpdateSalaryInput updateSalaryInput) {
        return this.employeeRepository.findById(updateSalaryInput.getEmployeeId())
                .flatMap(employee -> {
                    employee.setSalary(updateSalaryInput.getSalary());
                    return this.employeeRepository.save(employee);
                });
    }

    @QueryMapping
    public Flux<Department> allDepartment() {
        return this.departmentRepository.findAll();
    }

//    @SchemaMapping(typeName = "Department", field = "employees")
//    public Flux<Employee> employees(Department department) {
//        log.info("department id #{}", department.getId());
//        return this.employeeRepository.getAllEmployeeByDepartmentId(department.getId());
//    }

    @BatchMapping
    public Mono<Map<Department, Collection<Employee>>> employees(List<Department> departments) {
        return Flux.fromIterable(departments)
                .flatMap(department -> this.employeeRepository.getAllEmployeeByDepartmentId(department.getId()))
                .collectMultimap(employee -> departments.stream().filter(department -> department.getId().equals(employee.getDepartmentId())).findFirst().get());
    }


    @SubscriptionMapping
    public Flux<Employee> allEmployee() {
        return this.employeeRepository.findAll().delayElements(Duration.ofSeconds(3));
    }


    /*
    *
    * This method is a sample for a client call
    * */
    @GetMapping("/employeeByName")
    public Mono<List<Employee>> employeeByName() {
        var document = "query {\n" +
                "  employeeByName(employeeName: \"Rakesh\") {\n" +
                "    id\n" +
                "    name\n" +
                "    salary\n" +
                "  }\n" +
                "}";
        return this.httpGraphQlClient.document(document)
                .retrieve("employeeByName")
                .toEntityList(Employee.class);
    }
}
