package se.lab.cicd;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextUtilsTest {

    @Test
    void reversesText() {
        assertEquals("cba", TextUtils.reverse("abc"));
    }

    @Test
    void reverseOfNullIsNull() {
        assertNull(TextUtils.reverse(null));
    }

    @Test
    void recognisesSimplePalindromes() {
        assertTrue(TextUtils.isPalindrome("Anna"));
        assertTrue(TextUtils.isPalindrome("racecar"));
        assertFalse(TextUtils.isPalindrome("Java"));
    }

    // Lab exercise 5: remove @Disabled, push, watch CI fail, then fix TextUtils.isPalindrome.
    @Test
    void palindromeIgnoresSpacesAndPunctuation() {
        assertTrue(TextUtils.isPalindrome("Never odd or even"));
        assertTrue(TextUtils.isPalindrome("A man, a plan, a canal: Panama!"));
    }
}
