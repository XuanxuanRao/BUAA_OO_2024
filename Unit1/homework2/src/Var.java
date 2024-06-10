
public class Var implements Factor {
    private final Polynomial polynomial;

    public Var(String exponent) {
        polynomial = new Polynomial(new Monomial("1", exponent));
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
