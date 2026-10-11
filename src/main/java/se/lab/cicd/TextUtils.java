package se.lab.cicd;

/**
 * Small text helpers.
 */
public final class TextUtils {

    private TextUtils() {
    }

    /** Returns the text reversed, or null if the text is null. */
    public static String reverse(String text) {
        if (text == null) {
            return null;
        }
        return new StringBuilder(text).reverse().toString();
    }

    /**
     * Returns true if the text reads the same forwards and backwards.
     * Comparison ignores upper/lower case.
     */
    public static boolean isPalindrome(String text) {
        if (text == null) {
            return false;
        }
        String normalized = text.toLowerCase();
        return normalized.equals(reverse(normalized));
    }
}
