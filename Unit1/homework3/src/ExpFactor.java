public class ExpFactor implements Factor {
    private final Polynomial polynomial;
    private final Polynomial exponent;

    public ExpFactor(Polynomial exponent) {
        this.exponent = exponent;
        polynomial = new Polynomial(new Monomial("1", exponent));
    }

    @Override
    public String toString() {
        return "exp(" + exponent + ")";
    }

    @Override
    public Polynomial toPolynomial() {
        return polynomial;
    }
}
