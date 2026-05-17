package exceptions;

public class NotResearcherException extends Exception {

    private final String userId;

    public NotResearcherException(String userId) {
        super(String.format(
            "User '%s' is not a researcher and cannot perform this action.", userId
        ));
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}