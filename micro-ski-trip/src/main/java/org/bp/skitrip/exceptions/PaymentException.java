package org.bp.skitrip.exceptions;

public class PaymentException extends Exception {
        public PaymentException() {
        }

        public PaymentException(String message) {
            super(message);
        }

        public PaymentException(Throwable cause) {
            super(cause);
        }

        public PaymentException(String message, Throwable cause) {
            super(message, cause);
        }

        public PaymentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

}
