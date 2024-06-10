import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Polynomial {
    private final HashMap<Unit, BigInteger> monomials;
    public static final Polynomial ZERO = new Polynomial();
    public static final Polynomial ONE = new Polynomial(new Monomial("1", Unit.ONE));

    public Polynomial(Monomial monomial) {
        this.monomials = new HashMap<>();
        this.monomials.put(monomial.getUnit(), monomial.getCoefficient());
    }

    public Polynomial(Polynomial polynomial) {
        this.monomials = polynomial.monomials;
    }

    public Polynomial(HashMap<Unit, BigInteger> monomials) {
        this.monomials = new HashMap<>(monomials);
    }

    public Polynomial() {
        monomials = new HashMap<>();
    }

    public boolean isZero() {
        return monomials.isEmpty();
    }

    /**
     * 将该多项式加上另一个多项式
     * @param addend 要相加的多项式
     * @return this + addend
     */
    public Polynomial addPolynomial(Polynomial addend) {
        HashMap<Unit, BigInteger> result = new HashMap<>(this.monomials);
        addend.monomials.forEach((key, value) -> {
            BigInteger resCoefficient = result.getOrDefault(key, BigInteger.ZERO).add(value);
            if (resCoefficient.equals(BigInteger.ZERO)) {
                result.remove(key);
            } else {
                result.put(key, resCoefficient);
            }
        });
        return new Polynomial(result);
    }

    /**
     * 将当前多项式减去另一个多项式
     * @param subtrahend Polynomial to be subtracted from this Polynomial
     * @return this - subtrahend
     */
    public Polynomial subPolynomial(Polynomial subtrahend) {
        HashMap<Unit, BigInteger> result = new HashMap<>(this.monomials);
        subtrahend.monomials.forEach((key, value) -> {
            BigInteger resCoefficient = result.getOrDefault(key, BigInteger.ZERO).subtract(value);
            if (resCoefficient.equals(BigInteger.ZERO)) {
                result.remove(key);
            } else {
                result.put(key, resCoefficient);
            }
        });
        return new Polynomial(result);
    }

    /**
     * 将当前多项式乘上一个单项式
     * @param multiplier 要相乘的单项式
     * @return this * multiplier
     */
    public Polynomial mulMonomial(Monomial multiplier) {
        HashMap<Unit, BigInteger> result = new HashMap<>();
        this.monomials.forEach((key, value) -> {
            Unit resExponent = key.multiply(multiplier.getUnit());
            BigInteger resCoefficient = value.multiply(multiplier.getCoefficient())
                    .add(result.getOrDefault(resExponent, BigInteger.ZERO));
            if (!resCoefficient.equals(BigInteger.ZERO)) {
                result.put(resExponent, resCoefficient);
            } else {
                result.remove(resExponent);
            }
        });
        return new Polynomial(result);
    }

    /**
     * 将当前多项式乘上另一个多项式
     * @param multiplier 要相乘的多项式
     * @return this * multiplier
     */
    public Polynomial mulPolynomial(Polynomial multiplier) {
        Polynomial res = ZERO;
        for (Map.Entry<Unit, BigInteger> entry : multiplier.monomials.entrySet()) {
            res = res.addPolynomial(mulMonomial(new Monomial(entry.getValue(), entry.getKey())));
        }
        return res;
    }

    /**
     * Returns a Polynomial whose value is <code>(this<sup>exponent</sup>)</code>.
     *              Note that exponent is an integer rather than a Polynomial.
     * @param exponent exponent to which this Polynomial is to be raised.
     * @return <code>this<sup>exponent</sup></code>
     * @throws ArithmeticException {@code exponent} is negative.  (This would
     *             cause the operation to yield a non-integer value.)
     */
    public Polynomial pow(int exponent) {
        if (exponent < 0) {
            throw new ArithmeticException("Negative exponent");
        }
        Polynomial res = ONE;
        for (int i = 0; i < exponent; i++) {
            res = res.mulPolynomial(this);
        }
        return res;
    }

    public int size() {
        return monomials.size();
    }

    public HashMap<Unit, BigInteger> getMonomials() {
        return monomials;
    }

    public BigInteger gcdMultiple() {
        BigInteger result = BigInteger.ZERO;
        for (BigInteger value : monomials.values()) {
            if (value.equals(BigInteger.ZERO)) {
                continue;
            }
            if (result.equals(BigInteger.ZERO)) {
                result = value.abs();
            } else {
                result = result.gcd(value.abs());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Unit, BigInteger> entry : monomials.entrySet()) {
            sb.append(new Monomial(entry.getValue(), entry.getKey()));
            sb.append("+");
        }
        if (sb.length() == 0) {
            return "0";
        } else {
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }

    public String simplify(BigInteger index) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Unit, BigInteger> entry : monomials.entrySet()) {
            sb.append(new Monomial(entry.getValue().divide(index), entry.getKey()));
            sb.append("+");
        }
        if (sb.length() == 0) {
            return "0";
        } else {
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Polynomial other = (Polynomial) obj;
        return this.isZero() && other.isZero() || this.monomials.entrySet().stream()
                .allMatch(entry -> other.monomials.containsKey(entry.getKey())
                        && other.monomials.get(entry.getKey()).equals(entry.getValue()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(monomials);
    }
}
