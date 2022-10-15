package account.service;

import account.dto.Password;
import account.dto.PasswordChangingResponse;
import account.entity.User;
import account.exception.PasswordTooShortException;
import account.exception.SamePasswordException;
import account.exception.UserExistException;
import account.exception.VulnerablePassword;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Autowired
    private PasswordEvaluator passwordEvaluator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> signUp(User user) {

        passwordEvaluator.evaluatePassword(user.getPassword());

        if (checkIfUserAlreadyExists(user)) {
            throw new UserExistException();
        }

        encodePassword(user);
        userRepository.save(user);

        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    private User getUserByEmail(String email) {
        return userRepository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    private boolean checkIfUserAlreadyExists(User user) {
        return userRepository.findUserByEmailIgnoreCase(user.getEmail()).isPresent();
    }

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }


    public User getEmployeePayroll(User user) {

        User userInDB = userRepository.findUserByEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return userInDB;
    }

    public ResponseEntity<PasswordChangingResponse> changePass(User user, Password password) {

        passwordEvaluator.evaluatePassword(password.getNewPassword());

        User userInDB = getUserByEmail(user.getEmail());

        if (checkTheSamePassword(userInDB, password)) {
            throw new SamePasswordException();
        }

        String encryptedPassword = passwordEncoder.encode(password.getNewPassword());
        userInDB.setPassword(encryptedPassword);
        userRepository.save(userInDB);

        PasswordChangingResponse body = new PasswordChangingResponse(userInDB.getEmail(), "The password has been updated successfully");
        return new ResponseEntity<PasswordChangingResponse>(body, HttpStatus.OK);
    }

    private boolean checkTheSamePassword(User user, Password password) {
        return passwordEncoder.matches(password.getNewPassword(), user.getPassword());
    }
}
