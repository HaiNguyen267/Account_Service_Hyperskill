package account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModifyRoleRequest {

    @JsonProperty("user")
    private String userEmail;
    @JsonProperty("role")
    private String roleName;
    private String operation;

    public String getUserEmail() {
        return userEmail;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getOperation() {
        return operation;
    }
}
