package account.controller;

import account.entity.User;
import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


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
    public ResponseEntity<?> changePass() {
        return null;
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<?> getEmployeePayroll() {
        return null;
    }

    @PostMapping("/acct/payments")
    public ResponseEntity<?> uploadPayroll() {
        return null;
    }

    @PutMapping("/acct/payments")
    public ResponseEntity<?> updatePayment() {
        return null;
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
