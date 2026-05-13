package org.example.ash.exception;

import lombok.Getter;
import lombok.Setter;

public class AppException extends  RuntimeException{
    @Getter
    @Setter
    protected String code;

    @Getter
    @Setter
    protected int status = 500;

    public AppException(String message) {
        super(message);
    }

    public AppException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AppException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public AppException status(int status) {
        this.status = status;
        return this;
    }

    public String toString() {
        String s = this.getClass().getName();
        String message = getLocalizedMessage();
        if (message == null) {
            if (this.code != null) {
                message = String.valueOf(this.code);
            }
        } else if (this.code != null && !message.contains(String.valueOf(this.code))) {
            message = message + " (" + this.code + ")";
        }

        return message != null ? s + ": " + message : s;
    }

}
