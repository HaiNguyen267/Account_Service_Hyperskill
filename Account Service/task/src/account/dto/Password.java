package account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Size;


public class Password {
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    @JsonProperty("new_password")
    private String newPassword;

    public Password(String newPassword) {
        this.newPassword = newPassword;
    }

    public Password() {
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
