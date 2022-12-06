package account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Payroll {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @JsonIgnore
    private String employeeEmail;
    private String name;
    private String lastname;
    private String period;
    private String salary;



    public Payroll(String employeeEmail,String name, String lastName, String period, String salary) {
        this.employeeEmail = employeeEmail;
        this.name = name;
        this.lastname = lastName;
        this.period = period;
        this.salary = salary;
    }

    public Payroll() {
    }

    public String getName() {
        return name;
    }


    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastName) {
        this.lastname = lastName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }
}
