import java.util.ArrayList;

public class Term {
    private ArrayList<Factor> factors;
    private int sign;

    public Term() {
        this.factors = new ArrayList<>();
    }

    public Term(ArrayList<Factor> factors, int sign) {
        this.factors = factors;
        this.sign = sign;
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public Polynomial toPolynomial() {
        Polynomial polynomial = Polynomial.ONE;
        for (Factor factor : factors) {
            polynomial = polynomial.mulPolynomial(factor.toPolynomial());
        }
        return polynomial;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int getSign() {
        return sign;
    }

    @Override
    public String toString() {
        return this.toPolynomial().toString();
    }
}
