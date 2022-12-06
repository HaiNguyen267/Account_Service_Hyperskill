package account.config;


import account.dto.MyCustomErrorDTO;
import account.entity.User;
import account.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

import static account.security.Role.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private EventService eventService;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String USER = ROLE_USER.name();
        String ACCOUNTANT = ROLE_ACCOUNTANT.name();
        String ADMINISTRATOR = ROLE_ADMINISTRATOR.name();
        String AUDITOR = ROLE_AUDITOR.name();
        http
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman and the H2 console
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/signup", "/actuator/shutdown", "h2-console").permitAll()
                .antMatchers("/api/auth/changepass").hasAnyAuthority(USER, ACCOUNTANT, ADMINISTRATOR)
                .antMatchers("/api/empl/payment").hasAnyAuthority(USER,ACCOUNTANT)
                .antMatchers("/api/acct/**").hasAuthority(ACCOUNTANT)
                .antMatchers("/api/admin/**").hasAuthority(ADMINISTRATOR)
                .antMatchers("/api/security/events/**").hasAuthority(AUDITOR)
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            createAccessDeniedLog();

            MyCustomErrorDTO errorDTO = new MyCustomErrorDTO(
                    LocalDateTime.now().toString(),
                    FORBIDDEN.value(),
                    "Forbidden",
                    "Access Denied!",
                    request.getServletPath()
                    );
            ServletOutputStream out = response.getOutputStream();
            new ObjectMapper().writeValue(out, errorDTO);
            out.flush();
        };
    }

    private void createAccessDeniedLog() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        eventService.createAccessDeniedEvent(currentUser.getEmail());
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(
            ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

}
