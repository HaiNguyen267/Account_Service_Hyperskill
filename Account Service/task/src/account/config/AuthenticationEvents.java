package account.config;

import account.entity.User;
import account.repository.UserRepository;
import account.security.EventName;
import account.service.EventService;
import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;

import static account.security.EventName.BRUTE_FORCE;
import static account.security.EventName.LOCK_USER;
import static account.security.Role.ROLE_ADMINISTRATOR;

@Component
public class AuthenticationEvents {
    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        User user = (User) event.getAuthentication().getPrincipal();
        user.setLoginFailedCount(0);
        userService.save(user);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        // if there is so many login failed attempts of this email, the account will be locked
        String userEmail = event.getAuthentication().getName();

        User user = userService.getUserByEmail(userEmail);
        user.setLoginFailedCount(user.getLoginFailedCount() + 1);
//        System.out.println("login hihi");
//        System.out.println(user.getLoginFailedCount());
        if (user.getLoginFailedCount() < 5) {
            eventService.createLoginFailedEvent(user.getEmail());
        } else if (user.getLoginFailedCount() == 5) {
            // the admin won't be locked
            if (!userIsAdmin(user)) {
                user.setLocked(true);
            }
            eventService.createLoginFailedEvent(user.getEmail());
            eventService.createBruteForceEvent(user.getEmail());
            eventService.createLockUserEvent(user.getEmail());
        }
        userService.save(user);
    }

    private boolean userIsAdmin(User user) {

        return user.getRoles().contains(ROLE_ADMINISTRATOR);
    }

}
