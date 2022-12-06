package account.controller;

import account.dto.*;
import account.entity.Event;
import account.entity.Payroll;
import account.entity.User;
import account.service.EventService;
import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static account.security.EventName.*;


@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody User user, HttpServletRequest request) {
        return userService.signUp(user, request);
    }

    @PostMapping("/auth/changepass")
    public ResponseEntity<CustomResponse> changePass(@AuthenticationPrincipal User user, @RequestBody Password password, HttpServletRequest request) {

        return userService.changePass(user, password);
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<?> getEmployeePayroll(@AuthenticationPrincipal User user, @Nullable @RequestParam String period) {
        if (period == null) {
            List<Payroll> employeePayroll = userService.getEmployeePayroll(user);
            return new ResponseEntity<>(employeePayroll, HttpStatus.OK);
        } else {
            Payroll employeePayrollAtPeriod = userService.getEmployeePayrollAtPeriod(user, period);
            return new ResponseEntity<>(employeePayrollAtPeriod, HttpStatus.OK);
        }
    }




    @PostMapping("/acct/payments")
    public ResponseEntity<?> uploadPayment(@RequestBody @Valid List<PayrollDTO> list) {
        return userService.uploadPayroll(list);
    }

    @PutMapping("/acct/payments")
    public ResponseEntity<?> updatePayroll(@RequestBody @Valid PayrollDTO payrollDTO) {
        return userService.updatePayroll(payrollDTO);
    }

    @PutMapping("/admin/user/role")
    public ResponseEntity<?> changeUserRole(@RequestBody ModifyRoleRequest modifyRoleRequest) {
        return userService.modifyRole(modifyRoleRequest);
    }

    @DeleteMapping("/admin/user/{userEmail}")
    public ResponseEntity<?> deleteUser(@PathVariable String userEmail) {
        return userService.deleteUser(userEmail);
    }

    @GetMapping("/admin/user")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/admin/user/access")
    public ResponseEntity<?> changeUserAccess(@RequestBody ChangeAccessRequest changeAccessRequest) {
        return userService.changeUserAccess(changeAccessRequest);
    }

    @GetMapping("/security/events")
    public List<Event> getLoggedEvents() {
        return eventService.getLoggedEvents();
    }
}
