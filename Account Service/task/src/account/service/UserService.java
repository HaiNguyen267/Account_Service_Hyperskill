package account.service;

import account.entity.User;
import account.exception.UserExistException;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> signUp(User user) {
        if (checkIfUserAlreadyExists(user)) {
            throw new UserExistException();
        }

        encodePassword(user);
        userRepository.save(user);

        return new ResponseEntity<User>(user, HttpStatus.OK);
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
}
