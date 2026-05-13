package org.example.ash.exception;

public class AppCodeException extends AppException{
    private int status;
    protected AppCode appCode;


    public AppCodeException(AppCode appCode) {
        this(String.valueOf(appCode), appCode != null ? appCode.getMessage() : null);
    }

    public AppCodeException(String code, String message) {
        super(code, message);
    }

    public AppCodeException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
