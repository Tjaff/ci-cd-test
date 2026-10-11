package se.lab.cicd;

/**
 * Entry point. Run with: java -jar target/ci-cd-demo-&lt;version&gt;.jar
 */
public class App {

    public static void main(String[] args) {
        Calculator calc = new Calculator();
        System.out.println("CI/CD demo app");
        System.out.println("2 + 3 = " + calc.add(2, 3));
        System.out.println("10 / 4 = " + calc.divide(10, 4));
        System.out.println("Is 'Anna' a palindrome? " + TextUtils.isPalindrome("Anna"));
    }
}
