package account.service;

import account.dto.Password;
import account.dto.PayrollDTO;
import account.dto.CustomResponse;
import account.entity.Payroll;
import account.entity.User;
import account.exception.SamePasswordException;
import account.exception.UserExistException;
import account.repository.PayrollRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private PasswordEvaluator passwordEvaluator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    public ResponseEntity<?> signUp(User user) {

        passwordEvaluator.evaluatePassword(user.getPassword());

        if (checkIfEmailExists(user.getEmail())) {
            throw new UserExistException();
        }

        encodePassword(user);
        userRepository.save(user);

        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    private User getUserByEmail(String email) {
        return userRepository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
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
                // make sure an employee cannot have 2 payroll on the same period
                if (checkIfPayrollPeriodExists(payrollDTO.getEmployeeEmail(), payrollDTO.getPeriod())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A period cannot repeat for an employee email="+ payrollDTO.getEmployeeEmail());
                }
                persistPayroll(payrollDTO);
            }
        }
        
        CustomResponse body = new CustomResponse();
        body.setStatus("Added successfully!");

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public ResponseEntity<?> updatePayroll(PayrollDTO payrollDTO) {
        if (isValidPayroll(payrollDTO)) {
            Payroll payroll = payrollRepository.findPayrollOfEmployeeAtPeriod(
                    payrollDTO.getEmployeeEmail(),
                    payrollDTO.getPeriod()
                    ).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payroll not exists"));

            String formattedSalary = formatSalary(payrollDTO.getSalary());
            payroll.setSalary(formattedSalary);
            payrollRepository.save(payroll);

            CustomResponse body = new CustomResponse();
            body.setStatus("Updated successfully");
            return new ResponseEntity<>(body, HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error in updating the payroll");


    }
    private void persistPayroll(PayrollDTO payrollDTO) {
        try {
           User user = userRepository.findUserByEmailIgnoreCase(payrollDTO.getEmployeeEmail()).get();

           String formattedPeriod = formatPeriod(payrollDTO.getPeriod());
           String formattedSalary = formatSalary(payrollDTO.getSalary());
           Payroll payroll = new Payroll(payrollDTO.getEmployeeEmail(),
                   formattedPeriod,
                   formattedSalary);


           user.addPayroll(payroll);
           userRepository.save(user);
        } catch (ParseException e) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period");
        }
    }

    private String formatSalary(long salary) {

        String dollar = "";
        String cent = "";
        String salaryString = String.valueOf(salary);

        for (int i = salaryString.length() - 1; i >= 0; i--) {
            if (cent.length() < 2) {
                cent = cent + salaryString.charAt(i);
            } else {
                dollar = dollar + salaryString.charAt(i);
            }
        }

        if (dollar.isEmpty()) {
            dollar = "0";
        }

        if (cent.isEmpty()) {
            cent = "0";
        }

        return String.format("%s dollar(s) %s cent(s)", dollar, cent);

    }

    private String formatPeriod(String period) throws ParseException {

        SimpleDateFormat formatter1 = new SimpleDateFormat("MMMM-yyyy");
        Date parsedDate = formatter1.parse(period);
        String formattedDate = formatter1.format(parsedDate);

        return formattedDate;

    }
    private boolean isValidPayroll(PayrollDTO payrollDTO) {
        // check salary positive
        if (payrollDTO.getSalary() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary cannot be negative");
        }

        // check user email exists
        if (checkIfEmailExists(payrollDTO.getEmployeeEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not found for email="+ payrollDTO.getEmployeeEmail());
        }
        
        return true;
    }

    private boolean checkIfPayrollPeriodExists(String employeeEmail, String period) {
        return payrollRepository.findPayrollOfEmployeeAtPeriod(employeeEmail, period).isPresent();
    }

    public Payroll getEmployeePayrollAtPeriod(User user, String period) {

        List<Payroll> list = user.getPayrollList();

        try {
            String formattedPeriod = formatPeriod(period);
            return list.stream()
                    .filter(p -> p.getPeriod().equals(formattedPeriod))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not found payroll at period=" + period));

        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period");
        }
    }
}
