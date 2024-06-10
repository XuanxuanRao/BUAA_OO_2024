import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

/**
 * The basic opponent of polynomial.
 * exponentX(int) is the index of x, exponentE(polynomial) is the index of e
 */
public class Unit {
    private final BigInteger exponentX;
    private final Polynomial exponentE;
    public static final Unit ONE = new Unit(BigInteger.ZERO, Polynomial.ZERO);

    public Unit(BigInteger exponentX, Polynomial exponentE) {
        this.exponentX = exponentX;
        this.exponentE = exponentE;
    }

    public Unit multiply(Unit multiplier) {
        return new Unit(exponentX.add(multiplier.exponentX),
                exponentE.addPolynomial(multiplier.exponentE));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Unit other = (Unit) obj;
        return this.exponentX.equals(other.exponentX) && this.exponentE.equals(other.exponentE);
    }

    @Override
    public String toString() {
        if (exponentX.equals(BigInteger.ZERO) && exponentE.isZero()) {
            return "1";
        } else if (exponentX.equals(BigInteger.ZERO)) {
            return expForm();
        } else {
            String varPart = exponentX.equals(BigInteger.ONE) ? "x" : "x^" + exponentX;
            if (exponentE.isZero()) {
                return varPart;
            } else {
                return varPart + "*" + expForm();
            }
        }
    }

    private String simplifyExp() {
        BigInteger gcd = exponentE.gcdMultiple();
        if (gcd.equals(BigInteger.ONE)) {
            return "exp((" + exponentE + "))";
        } else {
            int q;
            BigInteger index = BigInteger.ZERO;
            String ans = "";
            for (q = 1; q < 10; q++) {
                if (gcd.mod(BigInteger.valueOf(q)).equals(BigInteger.ZERO)) {
                    String res = exponentE.simplify(gcd.divide(BigInteger.valueOf(q)));
                    if (ans.isEmpty() || res.length() < ans.length()) {
                        ans = res;
                        index = gcd.divide(BigInteger.valueOf(q));
                    }
                }
            }
            return "exp((" + ans + "))^" + index;
        }
    }

    private String expForm() {
        if (exponentE.size() != 1) {
            return simplifyExp();
        }
        Map.Entry<Unit, BigInteger> entry = exponentE.getMonomials().entrySet().iterator().next();
        Unit unit = entry.getKey();
        BigInteger coefficient = entry.getValue();
        if (unit.equals(Unit.ONE)) {
            return "exp(" + exponentE + ")";
        } else if (unit.exponentX.equals(BigInteger.ZERO) || unit.exponentE.equals(Polynomial.ZERO)) {
            if (coefficient.equals(BigInteger.ONE)) {
                return "exp(" + exponentE + ")";
            } else if (coefficient.signum() == -1) {
                return "exp((" + exponentE + "))";
            } else {
                return "exp(" + unit + ")^" + coefficient;
            }
        } else {
            if (coefficient.equals(BigInteger.ONE)) {
                return "exp(" + exponentE + ")";
            } else if (coefficient.signum() == -1) {
                return "exp((" + exponentE + "))";
            } else {
                return "exp(" + unit + ")^" + coefficient;
            }
        }
    }

    private String xForm() {
        if (exponentX.equals(BigInteger.ONE)) {
            return "x";
        } else {
            return "x^" + exponentX;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(exponentX, exponentE);
    }
}
