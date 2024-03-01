import java.math.BigInteger;

public class Var implements Factor {
    private int exponent;

    public Var(int exponent) {
        this.exponent = exponent;
    }

    @Override
    public Polynomial toPolynomial() {
        return new Polynomial(new Monomial(BigInteger.ONE, exponent));
    }

    @Override
    public String toString() {
        if (exponent == 0) {
            return "1";
        } else if (exponent == 1) {
            return "x";
        } else {
            return "x^" + exponent;
        }
    }
}
