public class ExprFactor implements Factor {
    private final Polynomial polynomial;

    public ExprFactor(Expr base, int exponent) {
        this.polynomial = base.toPolynomial().pow(exponent);
    }

    @Override
    public String toString() {
        return polynomial.toString();
    }

    @Override
    public Polynomial toPolynomial() {
        return polynomial;
    }
}
