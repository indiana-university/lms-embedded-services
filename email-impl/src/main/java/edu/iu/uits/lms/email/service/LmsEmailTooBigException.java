package edu.iu.uits.lms.email.service;

public class LmsEmailTooBigException extends Exception {
    public LmsEmailTooBigException() {
        super();
    }

    public LmsEmailTooBigException(String message) {
        super(message);
    }

    public LmsEmailTooBigException(String message, Throwable cause) {
        super(message, cause);
    }

    public LmsEmailTooBigException(Throwable cause) {
        super(cause);
    }

    protected LmsEmailTooBigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
