package account.service;

import account.dto.Password;
import account.entity.User;
import account.exception.PasswordTooShortException;
import account.exception.VulnerablePassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class PasswordEvaluator {
    @Autowired
    private PasswordEncoder passwordEncoder;


    final Set<String> BREACHED_PASSWORDS = new HashSet<String>(Arrays.asList("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"));

    public boolean checkVulnerablePassword(String password) {
        return BREACHED_PASSWORDS.contains(password);
    }

    public boolean checkTheSamePassword(User user, Password password) {
        return passwordEncoder.matches(password.getNewPassword(), user.getPassword());
    }

    public boolean checkIfPasswordTooShort(String password) {
        return password.length() < 12;
    }

    public void evaluatePassword(String password) {
        if (checkIfPasswordTooShort(password)) {
            throw new PasswordTooShortException();
        }
        if (checkVulnerablePassword(password)) {
            throw new VulnerablePassword();
        }

    }

}
