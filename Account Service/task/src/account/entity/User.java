package account.entity;

import account.security.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.*;
import java.util.stream.Collectors;


@Entity
public class User implements UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(User.class);
    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty
    private String name;

    @NotEmpty
    @JsonProperty("lastname")
    private String lastname;

    @NotEmpty
    @Pattern(regexp = "[^@]*(@acme.com)$")
    private String email;

    @Size(min = 12, message = "The password length must be at least 12 chars!")
    @NotEmpty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonIgnore
    @OneToMany()
    private List<Payroll> payrollList = new ArrayList<>();

    @JsonIgnore
    private int loginFailedCount = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<Role> roles = new ArrayList<>();

    private boolean isLocked;

    public User(String name, String lastName, String email, String password) {
        this.name = name;
        this.lastname = lastName;
        this.email = email;
        this.password = password;
    }

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastName) {
        this.lastname = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    public void addPayroll(Payroll payroll) {
        payrollList.add(payroll);
    }

    public List<Payroll> getPayrollList() {
        List<Payroll> reversed =  new ArrayList<>(payrollList);
        Collections.reverse(reversed);
        return reversed;
    }

    public void setPayrollList(List<Payroll> payrollList) {
        this.payrollList = payrollList;
    }



    public List<Role> getRoles() {
        roles.sort((a, b) -> a.name().compareTo(b.name()));
        return roles;
    }
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {


        List<GrantedAuthority> authorities = roles.stream().map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        return authorities;
    }

    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return email;
    }

    public int getLoginFailedCount() {
        return loginFailedCount;
    }

    public void setLoginFailedCount(int loginFailedCount) {
        this.loginFailedCount = loginFailedCount;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", name, lastname, email, password);

    }
}
