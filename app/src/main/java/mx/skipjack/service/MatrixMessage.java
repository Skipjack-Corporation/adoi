package mx.skipjack.service;

public enum MatrixMessage {
    ERROR_USER_IN_USE("Username already taken."),
    ERROR_EMAIL_IN_USE("This email address is already in use."),
    ERROR_VERIFYING_EMAIL("An error occurred while verifying your email address."),
    ERROR_UNAUTHORIZE_EMAIL_BIND("Unauthorize email binding. Please check server configuration."),
    ERROR_NO_NETWORK("You are not connected to any network. Please check your internet connection and try again.");


    private String message;
    MatrixMessage(String msg) {

        message = msg;
    }


    public String getMessage() {
        return message;
    }

}
