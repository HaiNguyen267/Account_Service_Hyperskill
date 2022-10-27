package account.controller;

import account.dto.Password;
import account.dto.PayrollDTO;
import account.dto.CustomResponse;
import account.entity.Payroll;
import account.entity.User;
import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody User user) {
        return userService.signUp(user);
    }

    @PostMapping("/auth/changepass")
    public ResponseEntity<CustomResponse> changePass(@AuthenticationPrincipal User user, @RequestBody Password password) {

        return userService.changePass(user, password);
    }

    @GetMapping("/empl/payment")
    public List<Payroll> getEmployeeAllPayrolls(@AuthenticationPrincipal User user) {
        return userService.getEmployeePayroll(user);
    }

    @GetMapping("/empl/payment")
    public Payroll getEmployeePayrollAtPeriod(@AuthenticationPrincipal User user, @RequestParam String period) {
        return userService.getEmployeePayrollAtPeriod(user, period);
    }

    @PostMapping("/acct/payments")
    public ResponseEntity<?> uploadPayroll(List<PayrollDTO> payrollList) {
        return userService.uploadPayroll(payrollList);
    }


    @PutMapping("/acct/payments")
    public ResponseEntity<?> updatePayment(@RequestBody PayrollDTO payrollDTO) {
        return userService.updatePayroll(payrollDTO);
    }

    @PutMapping("/admin/user/role")
    public ResponseEntity<?> changeUserRole() {
        return null;
    }

    @DeleteMapping("/admin/user")
    public ResponseEntity<?> deleteUser() {
        return null;
    }

    @GetMapping("/admin/user")
    public ResponseEntity<?> getAllUsers() {
        return null;
    }
}
