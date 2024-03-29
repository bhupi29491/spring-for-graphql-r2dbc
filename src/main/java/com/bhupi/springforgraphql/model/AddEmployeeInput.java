package com.bhupi.springforgraphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddEmployeeInput {

    private String name, salary;
    private Integer departmentId;
}
