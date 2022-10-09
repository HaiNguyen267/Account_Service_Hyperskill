package account.repository;

import account.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findUserByEmail(String email);

    void saveUser(User user);
}
