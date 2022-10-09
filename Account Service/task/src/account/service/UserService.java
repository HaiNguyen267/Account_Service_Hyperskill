package account.service;

import account.entity.User;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {



    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> signUp(User user) {
        // no checking duplicated users in this stage
//        if (checkIfUserAlreadyExists(user)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }

        encodePassword(user);
        userRepository.saveUser(user);

        return new ResponseEntity<User>(user, HttpStatus.OK);
    }



    private boolean checkIfUserAlreadyExists(User user) {
        return userRepository.findUserByEmail(user.getEmail()).isPresent();
    }

    private void encodePassword(User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }
}
