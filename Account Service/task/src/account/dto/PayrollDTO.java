package account.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;

public class PayrollDTO {
    @JsonProperty("employee")
    private String employeeEmail;
    private String period;
    @Min(value = 0L, message = "The salary must be a positive number")
    private long salary;

    public PayrollDTO(String employeeEmail, String period, long salary) {
        this.employeeEmail = employeeEmail;
        this.period = period;
        this.salary = salary;
    }

    public PayrollDTO() {
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail.toLowerCase();
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }
}
