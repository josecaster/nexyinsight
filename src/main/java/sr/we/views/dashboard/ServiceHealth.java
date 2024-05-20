package sr.we.views.dashboard;

import java.math.BigDecimal;

/**
 * Simple DTO class for the inbox list to demonstrate complex object data
 */
public class ServiceHealth {

    private byte[] profilePicture;

    private Status status;

    private String city;

    private BigDecimal input;

    private int output;

    private String theme;

    enum Status {
        EXCELLENT, OK, FAILING;
    }

    public ServiceHealth() {

    }

    public ServiceHealth(Status status, String city, BigDecimal input, int output) {
        this.status = status;
        this.city = city;
        this.input = input;
        this.output = output;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getInput() {
        return input;
    }

    public void setInput(BigDecimal input) {
        this.input = input;
    }

    public int getOutput() {
        return output;
    }

    public void setOutput(int output) {
        this.output = output;
    }

}
