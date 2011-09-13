package ch.bind.philib.validation;

public class SimpleValidation {

    private SimpleValidation() {
    }

    public static void notNegative(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }
    }

    public static void notNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }

    public static void notNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("object must not be null");
        }
    }

    public static void notNull(Object obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    public static void isTrue(boolean value) {
        if (!value) {
            throw new IllegalArgumentException("value must be true");
        }
    }

    public static void isTrue(boolean value, String name) {
        if (!value) {
            throw new IllegalArgumentException(name + " must be true");
        }
    }

    public static void isFalse(boolean value) {
        if (value) {
            throw new IllegalArgumentException("value must be false");
        }
    }

    public static void isFalse(boolean value, String name) {
        if (value) {
            throw new IllegalArgumentException(name + " must be false");
        }
    }
}
