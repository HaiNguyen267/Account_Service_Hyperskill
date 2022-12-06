package account.service;

import account.entity.Event;
import account.repository.EventRepository;
import account.security.EventName;
import account.security.Role;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static account.security.EventName.*;

@Service
public class EventService {
//TODO: move the getCurrentAdmin to this class
    //TODO: implements the rest 3 log events
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void createEvent(EventName eventName, String subject, String object) {
        eventRepository.save(new Event(
                new java.sql.Date(System.currentTimeMillis()),
                eventName,
                subject,
                object,
                getCurrentPath())
        );

    }

    public void createCreateUserEvent(String userEmail) {
        createEvent(CREATE_USER, "Anonymous", userEmail);
    }

    public void createLoginFailedEvent(String userEmail) {
        createEvent(LOGIN_FAILED, userEmail, getCurrentPath());
    }

    public void createGrantRoleEvent(String adminEmail, Role role, String userEmail) {
        createEvent(GRANT_ROLE, adminEmail, String.format("Grant role %s to %s", role.getName().toUpperCase(), userEmail.toLowerCase()));
    }

    public void createRemoveRoleEvent(String adminEmail, Role role, String userEmail) {
        createEvent(REMOVE_ROLE, adminEmail, String.format("Remove role %s from %s", role.getName().toUpperCase(), userEmail.toLowerCase()));
    }


    public void createDeleteUserEvent(String adminEmail, String userEmail) {
        createEvent(DELETE_USER, adminEmail, userEmail);
    }

    public void createChangePasswordEvent(String userEmail) {
        createEvent(CHANGE_PASSWORD, userEmail, userEmail);
    }

    public void createAccessDeniedEvent(String userEmail) {
        createEvent(ACCESS_DENIED, userEmail, getCurrentPath());
    }

    public void createLockUserEvent(String userEmail) {
        createEvent(LOCK_USER, userEmail, String.format("Lock user %s", userEmail));
    }

    public void createUnlockUserEvent(String adminEmail, String userEmail) {
        createEvent(UNLOCK_USER, adminEmail, String.format("Unlock user %s", userEmail));
    }

    public void createBruteForceEvent(String userEmail) {
        createEvent(BRUTE_FORCE, userEmail, getCurrentPath());
    }

    private String getCurrentPath() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        return request.getServletPath();
    }
    public List<Event> getLoggedEvents() {
        return eventRepository.findAll();
    }
}
