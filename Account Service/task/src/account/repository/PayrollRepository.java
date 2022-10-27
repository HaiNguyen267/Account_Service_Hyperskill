package account.repository;

import account.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    @Query("SELECT p FROM Payroll p WHERE p.employeeEmail = ?1")
    List<Payroll> findPayrollsOfEmployees(String email);

    @Query("SELECT p FROM Payroll p WHERE p.employeeEmail = ?1 AND p.period = ?2")
    Optional<Payroll> findPayrollOfEmployeeAtPeriod(String email, String period);
}
