package account.dto;

public class PasswordChangingResponse {
    private String email;
    private String status;

    public PasswordChangingResponse(String email, String status) {
        this.email = email;
        this.status = status;
    }

    public PasswordChangingResponse() {
    }

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
