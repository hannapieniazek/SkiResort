package org.bp.skitrip.exceptions;

public class SkiEquipmentException extends Exception {
        public SkiEquipmentException() {
        }

        public SkiEquipmentException(String message) {
            super(message);
        }

        public SkiEquipmentException(Throwable cause) {
            super(cause);
        }

        public SkiEquipmentException(String message, Throwable cause) {
            super(message, cause);
        }

        public SkiEquipmentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

    }