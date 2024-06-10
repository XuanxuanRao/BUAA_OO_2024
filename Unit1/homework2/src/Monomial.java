import java.math.BigInteger;

public class Monomial {
    private final BigInteger coefficient;
    private final Unit unit;

    public Monomial(BigInteger coefficient, Unit unit) {
        this.unit = unit;
        this.coefficient = new BigInteger(coefficient.toString());
    }

    public Monomial(String coefficient, Unit unit) {
        this.unit = unit;
        this.coefficient = new BigInteger(coefficient);
    }

    /**
     * create a Monomial: (coefficient * <code>x<sup>exponent</sup></code>)
     * @param coefficient  系数
     * @param exponentX    次数
     */
    public Monomial(String coefficient, String exponentX) {
        this.unit = new Unit(new BigInteger(exponentX), Polynomial.ZERO);
        this.coefficient = new BigInteger(coefficient);
    }

    public Monomial(String coefficient, Polynomial exponentE) {
        this.unit = new Unit(BigInteger.ZERO, exponentE);
        this.coefficient = new BigInteger(coefficient);
    }

    public Unit getUnit() {
        return unit;
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    @Override
    public String toString() {
        if (coefficient.equals(BigInteger.ZERO)) {
            return "0";
        } else if (coefficient.equals(BigInteger.ONE)) {
            return unit.toString();
        } else if (coefficient.equals(BigInteger.valueOf(-1L))) {
            return "-" + unit.toString();
        } else {
            if (unit.equals(Unit.ONE)) {
                return coefficient.toString();
            } else {
                return coefficient + "*" + unit.toString();
            }
        }
    }

}
