package se.lab.cicd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    private final Calculator calc = new Calculator();

    @Test
    void addsTwoNumbers() {
        assertEquals(5, calc.add(2, 3));
    }

    @Test
    void subtractsTwoNumbers() {
        assertEquals(-1, calc.subtract(2, 3));
    }

    @Test
    void multipliesTwoNumbers() {
        assertEquals(12, calc.multiply(3, 4));
    }

    @Test
    void dividesTwoNumbers() {
        assertEquals(2.5, calc.divide(10, 4));
    }

    @Test
    void divisionByZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> calc.divide(1, 0));
    }
}
