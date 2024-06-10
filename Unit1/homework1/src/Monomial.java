import java.math.BigInteger;

public class Monomial {
    private final BigInteger coefficient;
    private final int exponent;

    public Monomial(BigInteger coefficient, int exponent) {
        this.exponent = exponent;
        this.coefficient = new BigInteger(coefficient.toString());
    }

    public Monomial(String coefficient, int exponent) {
        this.exponent = exponent;
        this.coefficient = new BigInteger(coefficient);
    }

    public int getExponent() {
        return exponent;
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public String show(boolean flag) {
        if (exponent == 0) {
            return flag ? "1" : "";
        } else if (exponent == 1) {
            return "x";
        } else {
            return "x^" + exponent;
        }
    }

    @Override
    public String toString() {
        if (coefficient.equals(BigInteger.ZERO)) {
            return "0";
        } else if (coefficient.equals(BigInteger.ONE)) {
            return show(true);
        } else if (coefficient.equals(BigInteger.valueOf(-1L))) {
            return "-" + show(true);
        } else {
            String literalPart = show(false);
            if (literalPart.length() == 0) {
                return coefficient.toString();
            } else {
                return coefficient + "*" + literalPart;
            }
        }
    }

}
