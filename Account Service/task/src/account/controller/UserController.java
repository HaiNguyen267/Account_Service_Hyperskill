package account.controller;

import account.dto.LoginCredential;
import account.dto.Password;
import account.dto.PasswordChangingResponse;
import account.entity.User;
import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<PasswordChangingResponse> changePass(@AuthenticationPrincipal User user,@RequestBody Password password) {

        return userService.changePass(user, password);
    }

    @GetMapping("/empl/payment")
    public User getEmployeePayroll(@AuthenticationPrincipal User user) {
        return userService.getEmployeePayroll(user);
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
