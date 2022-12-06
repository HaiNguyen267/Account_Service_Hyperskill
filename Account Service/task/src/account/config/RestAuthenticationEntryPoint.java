package account.config;

import account.dto.MyCustomErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // if authentication failed due to locked account
        if (authException.getClass().equals(LockedException.class)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(APPLICATION_JSON_VALUE);
            MyCustomErrorDTO body = new MyCustomErrorDTO(
                    LocalDateTime.now().toString(),
                    UNAUTHORIZED.value(),
                    "Unauthorized",
                    "User account is locked",
                    request.getServletPath()
            );
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        } else {
            // if authentication failed due to incorrect credentials
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());

        }
    }

}

