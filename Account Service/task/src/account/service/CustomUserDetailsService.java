package account.service;

import account.entity.User;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByEmailIgnoreCase(username);

        if (user.isEmpty()) {
            eventService.createLoginFailedEvent(username);
            throw new UsernameNotFoundException("Not found user for email: " + username);
        }
        // this is the object you get when using @AuthenticationPrincipal or SecurityContext.getContext().getAuthentication().getPrincipal() to get the current logged in user
        return user.get();
    }
}
