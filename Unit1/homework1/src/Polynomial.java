import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Polynomial {
    private HashMap<Integer, BigInteger> monomials = new HashMap<>();
    public static final Polynomial ZERO = new Polynomial(new Monomial("0", 0));
    public static final Polynomial ONE = new Polynomial(new Monomial("1", 0));

    public Polynomial(Monomial monomial) {
        monomials.put(monomial.getExponent(), monomial.getCoefficient());
    }

    public Polynomial(HashMap<Integer, BigInteger> monomials) {
        this.monomials = monomials;
    }

    public Polynomial addPolynomial(Polynomial addend) {
        HashMap<Integer, BigInteger> monomials =
                (HashMap<Integer, BigInteger>) this.monomials.clone();
        for (Map.Entry<Integer, BigInteger> entry : addend.monomials.entrySet()) {
            BigInteger resCoefficient = this.monomials.
                    getOrDefault(entry.getKey(), BigInteger.ZERO).add(entry.getValue());
            if (resCoefficient.equals(BigInteger.ZERO)) {
                monomials.remove(entry.getKey());
            } else {
                monomials.put(entry.getKey(), resCoefficient);
            }
        }
        return new Polynomial(monomials);
    }

    public Polynomial subPolynomial(Polynomial subtrahend) {
        HashMap<Integer, BigInteger> monomials =
                (HashMap<Integer, BigInteger>) this.monomials.clone();
        for (Map.Entry<Integer, BigInteger> entry : subtrahend.monomials.entrySet()) {
            BigInteger resCoefficient = this.monomials.
                    getOrDefault(entry.getKey(), BigInteger.ZERO).subtract(entry.getValue());
            if (resCoefficient.equals(BigInteger.ZERO)) {
                monomials.remove(entry.getKey());
            } else {
                monomials.put(entry.getKey(), resCoefficient);
            }
        }
        return new Polynomial(monomials);
    }

    public Polynomial mulMonomial(Monomial multiplier) {
        HashMap<Integer, BigInteger> monomials = new HashMap<>();
        for (Map.Entry<Integer, BigInteger> entry : this.monomials.entrySet()) {
            int resExponent = entry.getKey() + multiplier.getExponent();
            BigInteger resCoefficient = entry.getValue().multiply(multiplier.getCoefficient()).
                    add(monomials.getOrDefault(resExponent, BigInteger.ZERO));
            if (!resCoefficient.equals(BigInteger.ZERO)) {
                monomials.put(resExponent, resCoefficient);
            } else {
                monomials.remove(resExponent);
            }
        }
        return new Polynomial(monomials);
    }

    public Polynomial mulPolynomial(Polynomial multiplier) {
        Polynomial res = ZERO;
        for (Map.Entry<Integer, BigInteger> entry : multiplier.monomials.entrySet()) {
            res = res.addPolynomial(mulMonomial(new Monomial(entry.getValue(), entry.getKey())));
        }
        return res;
    }

    public Polynomial pow(int pow) {
        Polynomial res = ONE;
        for (int i = 0; i < pow; i++) {
            res = res.mulPolynomial(this);
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, BigInteger> entry : monomials.entrySet()) {
            sb.append(new Monomial(entry.getValue(), entry.getKey()));
            sb.append("+");
        }
        if (sb.length() == 0) {
            return "0";
        } else {
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }
}
