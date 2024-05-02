package sr.we.integration;

public class Error {

    public enum Type {
        INTERNAL_SERVER_ERROR(500),
        BAD_REQUEST(400),
        INCORRECT_VALUE_TYPE(400),
        MISSING_REQUIRED_PARAMETER(400),
        INVALID_VALUE(400),
        INVALID_RANGE(400),
        INVALID_CURSOR(400),
        CONFLICTING_PARAMETERS(400),
        UNAUTHORIZED(401),
        PAYMENT_REQUIRED(402),
        FORBIDDEN(403),
        NOT_FOUND(404),
        UNSUPPORTED_MEDIA_TYPE(415),
        RATE_LIMITED(429),
        ;
        private Integer errorCode;

        Type(Integer errorCode) {
            this.errorCode = errorCode;
        }
    }

    private Type code;

    private String details;

    public Type getCode() {
        return code;
    }

    public void setCode(Type code) {
        this.code = code;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
