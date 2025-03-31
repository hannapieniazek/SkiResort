package org.bp.skitrip.exceptions;

public class SkiPassException extends Exception {
    public SkiPassException() {
    }

    public SkiPassException(String message) {
        super(message);
    }

    public SkiPassException(Throwable cause) {
        super(cause);
    }

    public SkiPassException(String message, Throwable cause) {
        super(message, cause);
    }

    public SkiPassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
