import java.math.BigInteger;

public class Number implements Factor {
    private final Polynomial polynomial;

    public Number(BigInteger val) {
        polynomial = new Polynomial(new Monomial(val, Unit.ONE));
    }

    public Number(String val) {
        polynomial = new Polynomial(new Monomial(val, Unit.ONE));
    }

    public Number(String val, int sign) {
        if (sign == 1) {
            polynomial = new Polynomial(new Monomial(val, "0"));
        } else {
            polynomial = new Polynomial(new Monomial(new BigInteger(val).negate(), Unit.ONE));
        }
    }

    @Override
    public Polynomial toPolynomial() {
        return polynomial;
    }

    @Override
    public String toString() {
        return polynomial.toString();
    }

}
