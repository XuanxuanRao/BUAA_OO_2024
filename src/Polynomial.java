import java.math.BigInteger;
import java.util.ArrayList;

public class Polynomial {
    private ArrayList<Monomial> monomials;
    public static final Polynomial ZERO = new Polynomial(new Monomial("0", 0));

    public Polynomial() {
        monomials = new ArrayList<>();
    }

    public Polynomial(ArrayList<Monomial> monomials) {
        this.monomials = monomials;
    }

    public Polynomial(Monomial monomial) {
        monomials = new ArrayList<>();
        monomials.add(monomial);
    }

    public void addMonomial(Monomial addend) {
        if (addend.getCoefficient().equals(BigInteger.ZERO)) {
            return;
        }
        for (int i = 0; i < monomials.size(); i++) {
            Monomial res = monomials.get(i).addMonomial(addend);
            if (res != null) {
                if (res.getCoefficient().equals(BigInteger.ZERO)) {
                    monomials.remove(i);
                } else {
                    monomials.set(i, res);
                }
                return;
            }
        }
        monomials.add(addend);
    }

    public void subMonomial(Monomial subtrahend) {
        if (subtrahend.getCoefficient().equals(BigInteger.ZERO)) {
            return;
        }
        for (int i = 0; i < monomials.size(); i++) {
            Monomial res = monomials.get(i).subMonomial(subtrahend);
            if (res != null) {
                if (res.getCoefficient().equals(BigInteger.ZERO)) {
                    monomials.remove(i);
                } else {
                    monomials.set(i, res);
                }
                return;
            }
        }
        monomials.add(new Monomial(subtrahend.getCoefficient().negate(), subtrahend.getExponent()));
    }

    public void addPolynomial(Polynomial addend) {
        for (Monomial monomial : addend.monomials) {
            this.addMonomial(monomial);
        }
    }

    public void subPolynomial(Polynomial subtrahend) {
        for (Monomial monomial : subtrahend.monomials) {
            this.subMonomial(monomial);
        }
    }

    public static Polynomial mulPolynomial(Polynomial polynomial1, Polynomial polynomial2) {
        if (polynomial1.monomials.isEmpty()) {
            return ZERO;
        } else if (polynomial2.monomials.isEmpty()) {
            return ZERO;
        } else {
            Polynomial res = new Polynomial();
            for (Monomial monomial1 : polynomial1.monomials) {
                for (Monomial monomial2 : polynomial2.monomials) {
                    res.addMonomial(monomial1.mulMonomial(monomial2));
                }
            }
            return res;
        }
    }

    public static Polynomial powPolynomial(Polynomial polynomial, int pow) {
        if (pow == 0) {
            return new Polynomial(new Monomial(BigInteger.ONE, 0));
        }
        Polynomial ans = polynomial;
        for (int i = 0; i < pow - 1; i++) {
            ans = mulPolynomial(ans, polynomial);
        }
        return ans;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Monomial monomial : monomials) {
            sb.append(monomial);
            sb.append('+');
        }
        if (sb.length() == 0) {
            return "0";
        } else {
            return sb.deleteCharAt(sb.length() - 1).toString();
        }
    }
}
