import java.math.BigInteger;
import java.util.ArrayList;

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

    /**
     * When the exponent are same,
     * return a Monomial whose coefficient is this + addend and exponent is same to this.
     * When the exponent are different,
     * return null as they cannot be calculated.
     * @param addend 
     * @return
     */
    public Monomial addMonomial(Monomial addend) {
        if (this.exponent != addend.exponent) {
            return null;
        } else {
            return new Monomial(this.coefficient.add(addend.coefficient), exponent);
        }
    }

    /**
     * When the exponent are same,
     * return a Monomial whose coefficient is this - addend and exponent is same to this.
     * When the exponent are different,
     * return null as they cannot be calculated.
     * @param subtrahend
     * @return
     */
    public Monomial subMonomial(Monomial subtrahend) {
        if (this.exponent != subtrahend.exponent) {
            return null;
        } else {
            return new Monomial(this.coefficient.subtract(subtrahend.coefficient), exponent);
        }
    }

    /**
     * Return a Monomial whose coefficient is this × multiplier and exponent is this + multiplier
     * @param multiplier
     * @return
     */
    public Monomial mulMonomial(Monomial multiplier) {
        return new Monomial(this.coefficient.multiply(multiplier.coefficient),
                this.exponent + multiplier.exponent);
    }

    /**
     * Convert this monomial into a polynomial with only one term
     * @return
     */
    public Polynomial toPolynomial() {
        ArrayList<Monomial> monomials = new ArrayList<>();
        monomials.add(this);
        return new Polynomial(monomials);
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
