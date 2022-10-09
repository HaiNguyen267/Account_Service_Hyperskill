package account.repository;

import account.entity.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private List<User> list = new ArrayList<>();
    @Override
    public Optional<User> findUserByEmail(String email) {
        return list.stream()
                .filter(user -> user.getEmail()
                .equals(email)).findFirst();
    }

    @Override
    public void saveUser(User user) {
        list.add(user);
    }
}
