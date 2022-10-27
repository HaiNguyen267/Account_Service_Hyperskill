package account.dto;


public class PayrollDTO {
    private String employeeEmail;
    private String period;
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
        this.employeeEmail = employeeEmail;
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
