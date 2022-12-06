package account.service;

import account.dto.*;
import account.entity.Payroll;
import account.entity.User;
import account.exception.SamePasswordException;
import account.exception.UserExistException;
import account.repository.PayrollRepository;
import account.repository.UserRepository;
import account.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static account.security.EventName.*;
import static account.security.Role.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;


@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private PasswordEvaluator passwordEvaluator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EventService eventService;

    public ResponseEntity<?> signUp(User user, HttpServletRequest request) {

        passwordEvaluator.evaluatePassword(user.getPassword());

        if (checkIfEmailExists(user.getEmail())) {
            throw new UserExistException();
        }

        encodePassword(user);
        assignRoleToUser(user);
        userRepository.save(user);

        eventService.createCreateUserEvent(user.getEmail());
//        eventService.createEvent(CREATE_USER, "Anonymous", user.getEmail(), request.getServletPath());

        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    private void assignRoleToUser(User user) {
        Role role;
        if (isTheFirstUser()) {
            role = getRoleByName("ADMINISTRATOR");
        } else {
            role = getRoleByName("USER");
        }
        user.getRoles().add(role);
    }

    private boolean isTheFirstUser() {
        return userRepository.findAll().isEmpty();
    }

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
    }

    private boolean checkIfEmailExists(String email) {
        return userRepository.findUserByEmailIgnoreCase(email).isPresent();
    }

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    public List<Payroll> getEmployeePayroll(User user) {

        User userInDB = userRepository.findUserByEmailIgnoreCase(user.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        List<Payroll> list = userInDB.getPayrollList();
        return list;
    }

    public ResponseEntity<CustomResponse> changePass(User user, Password password) {

        passwordEvaluator.evaluatePassword(password.getNewPassword());

        User userInDB = getUserByEmail(user.getEmail());

        if (checkTheSamePassword(userInDB, password)) {
            throw new SamePasswordException();
        }


        String encryptedPassword = passwordEncoder.encode(password.getNewPassword());
        userInDB.setPassword(encryptedPassword);

        userRepository.save(userInDB);

        eventService.createChangePasswordEvent(user.getEmail());
        CustomResponse body = new CustomResponse(userInDB.getEmail(), "The password has been updated successfully");
        return new ResponseEntity<CustomResponse>(body, HttpStatus.OK);
    }

    private boolean checkTheSamePassword(User user, Password password) {
        return passwordEncoder.matches(password.getNewPassword(), user.getPassword());
    }

    @Transactional
    public ResponseEntity<?> uploadPayroll(List<PayrollDTO> payrollDTOList) {
        for (PayrollDTO payrollDTO : payrollDTOList) {
            if (isValidPayroll(payrollDTO)) {

                persistPayroll(payrollDTO);


            }
        }

        CustomResponse body = new CustomResponse();
        body.setStatus("Added successfully!");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private void persistPayroll(PayrollDTO payrollDTO) {
        try {
            String employeeEmail = payrollDTO.getEmployeeEmail();

            String formattedPeriod = formatPeriod(payrollDTO.getPeriod());
            User user = getUserByEmail(payrollDTO.getEmployeeEmail());

            boolean duplicatedPayroll = checkIfPayrollPeriodExists(employeeEmail, formattedPeriod);
            if (duplicatedPayroll) {
                throw new ResponseStatusException(BAD_REQUEST, "A period cannot repeat for an employee email=" + payrollDTO.getEmployeeEmail());
            }

            String formattedSalary = formatSalary(payrollDTO.getSalary());
            Payroll payroll = new Payroll(
                    user.getEmail(),
                    user.getName(),
                    user.getLastname(),
                    formattedPeriod,
                    formattedSalary);

            user.addPayroll(payroll);
            payrollRepository.save(payroll);
            userRepository.save(user);
        } catch (ParseException e) {
            // the period is validated already before this method
        }


    }

    public ResponseEntity<?> updatePayroll(PayrollDTO payrollDTO) {
        try {
            if (isValidPayroll(payrollDTO)) {
                String formattedPeriod = formatPeriod(payrollDTO.getPeriod());
                String formattedSalary = formatSalary(payrollDTO.getSalary());

                Payroll payroll = payrollRepository.findPayrollOfEmployeeAtPeriod(
                        payrollDTO.getEmployeeEmail(),
                        formattedPeriod
                ).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Payroll not exists"));

                User user = getUserByEmail(payrollDTO.getEmployeeEmail());

//                if (payroll == null) {
//                     payroll = new Payroll(
//                            user.getEmail(),
//                            user.getName(),
//                            user.getLastname(),
//                            formattedPeriod,
//                            formattedSalary);
//
//                    payrollRepository.save(payroll);
//
//                    user.addPayroll(payroll);
//                    userRepository.save(user);
//                } else {
//                }

                payroll.setSalary(formattedSalary);
                payrollRepository.save(payroll);
                CustomResponse body = new CustomResponse();
                body.setStatus("Updated successfully!");
                return new ResponseEntity<>(body, HttpStatus.OK);
            }
        } catch (ParseException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid period");
        }

        throw new ResponseStatusException(BAD_REQUEST, "Error in updating the payroll");


    }


    private String formatSalary(long salary) {

        StringBuilder dollar = new StringBuilder();
        StringBuilder cent = new StringBuilder();
        String salaryString = String.valueOf(salary);

        for (int i = salaryString.length() - 1; i >= 0; i--) {
            if (cent.length() < 2) {
                cent.append(salaryString.charAt(i));
            } else {
                dollar.append(salaryString.charAt(i));
            }
        }

        if (dollar.length() == 0) {
            dollar = new StringBuilder("0");
        }

        if (cent.length() == 0) {
            cent = new StringBuilder("0");
        }

        return String.format("%s dollar(s) %s cent(s)", dollar.reverse().toString(), cent.reverse().toString());

    }

    private String formatPeriod(String period) throws ParseException {

        SimpleDateFormat firstFormat = new SimpleDateFormat("MM-yyyy");
        firstFormat.setLenient(false);
        Date parsedDate = firstFormat.parse(period);

        //TODO: check if it can format "13-2020"
        SimpleDateFormat secondFormat = new SimpleDateFormat("MMMM-yyyy");
        String formattedDate = secondFormat.format(parsedDate);

        return formattedDate;

    }


    private boolean isValidPayroll(PayrollDTO payrollDTO) {
        // make sure an employee cannot have 2 payroll on the same period


        try {
            String employeeEmail = payrollDTO.getEmployeeEmail();
            String formattedPeriod = formatPeriod(payrollDTO.getPeriod());


            if (payrollDTO.getSalary() < 0) {
                throw new ResponseStatusException(BAD_REQUEST, "The salary must be a positive number");
            }

            // check user email exists
            if (!checkIfEmailExists(employeeEmail)) {
                throw new ResponseStatusException(BAD_REQUEST, "Employee not found for email=" + payrollDTO.getEmployeeEmail());
            }

        } catch (ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(BAD_REQUEST, "Invalid period");
        }


        return true;
    }

    private boolean checkIfPayrollPeriodExists(String employeeEmail, String period) throws ParseException {
        return payrollRepository.findPayrollOfEmployeeAtPeriod(employeeEmail, period).isPresent();
    }

    public Payroll getEmployeePayrollAtPeriod(User user, String period) {

        List<Payroll> list = payrollRepository.findAllPayrollOfEmployee(user.getEmail());

        try {
            String formattedPeriod = formatPeriod(period);
            return list.stream()
                    .filter(p -> p.getPeriod().equals(formattedPeriod))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Not found payroll at period=" + period));

        } catch (ParseException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid period");
        }
    }

    public ResponseEntity<?> modifyRole(ModifyRoleRequest modifyRoleRequest) {
        User user = getUserByEmail(modifyRoleRequest.getUserEmail());
        Role role = getRoleByName(modifyRoleRequest.getRoleName());
        List<Role> roles = user.getRoles();

        // only admin get access to this method
        User currentAdminUser = getCurrentAdminUser();

        if (modifyRoleRequest.getOperation().equals("GRANT")) {
            if (isCombineOfAdminAndBusinessRole(roles, role)) {
                throw new ResponseStatusException(BAD_REQUEST, "The user cannot combine administrative and business roles!");
            }

            addRoleToUser(user, role);
            userRepository.save(user);



            eventService.createGrantRoleEvent(currentAdminUser.getEmail(), role, user.getEmail());
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            // REVOKE

            // cannot revoke the role user does not have
            if (!roles.contains(role)) {
                throw new ResponseStatusException(BAD_REQUEST, "The user does not have a role!");
            }

            // cannot revoke the role admin
            if (roles.contains(ROLE_ADMINISTRATOR)) {
                throw new ResponseStatusException(BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }

            // cannot revoke the only role user has
            if (roles.size() == 1) {
                throw new ResponseStatusException(BAD_REQUEST, "The user must have at least one role!");
            }


            roles.remove(role);
            eventService.createRemoveRoleEvent(currentAdminUser.getEmail(), role, user.getEmail());
            userRepository.save(user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    private User getCurrentAdminUser() {
        User admin = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return admin;
    }

    private void addRoleToUser(User user, Role role) {
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
        }
    }

    private boolean isCombineOfAdminAndBusinessRole(List<Role> roles, Role role) {
        // ADMINISTRATOR is in group of admin role
        // USER and ACCOUNTANT are int group of business roles
        // an user cannot have 2 groups of role at the same time
        boolean isAdmin = roles.contains(ROLE_ADMINISTRATOR); // if an user is admin, then he can not have any other roles
        boolean wantToBeAdmin = role == ROLE_ADMINISTRATOR; // there is only one admin who is the first registered user,

        return isAdmin || wantToBeAdmin;
    }

    public Role getRoleByName(String name) {
        for (Role role : Role.values()) {
            if (role.getName().equals(name)) {
                return role;
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
    }

    public ResponseEntity<?> deleteUser(String userEmail) {

        User user = getUserByEmail(userEmail);
        if (user.getRoles().contains(ROLE_ADMINISTRATOR)) {
            throw new ResponseStatusException(BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }


        userRepository.delete(user);

        // only admin has the access to this method
        User admin = getCurrentAdminUser();
        eventService.createDeleteUserEvent(admin.getEmail(), user.getEmail());

        Map<String, String> map = new LinkedHashMap<>();
        map.put("user", user.getEmail());
        map.put("status", "Deleted successfully!");

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public ResponseEntity<?> changeUserAccess(ChangeAccessRequest changeAccessRequest) {
        User user = getUserByEmail(changeAccessRequest.getUserEmail());
        String operation = changeAccessRequest.getOperation().toLowerCase();

        if (operation.equals("lock")) {
            if (user.getRoles().contains(ROLE_ADMINISTRATOR)) {
                throw new ResponseStatusException(BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
            }

            user.setLocked(true);
            userRepository.save(user);

            eventService.createLockUserEvent(user.getEmail());
        } else {
            user.setLocked(false);
            user.setLoginFailedCount(0);
            userRepository.save(user);

            User admin = getCurrentAdminUser();
            eventService.createUnlockUserEvent(admin.getEmail(),user.getEmail());
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("status", String.format("User %s %sed!", user.getEmail(), operation));
        return new ResponseEntity<>(map, OK);
    }

    public void save(User user) {
        userRepository.save(user);
    }
}
