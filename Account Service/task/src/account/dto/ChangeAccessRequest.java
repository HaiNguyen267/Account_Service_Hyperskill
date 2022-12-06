package account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangeAccessRequest {
    @JsonProperty("user")
    private String userEmail;
    private String operation;

    public ChangeAccessRequest(String userEmail, String operation) {
        this.userEmail = userEmail;
        this.operation = operation;
    }

    public ChangeAccessRequest() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getOperation() {
        return operation;
    }
}
