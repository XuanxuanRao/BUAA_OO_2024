public class ExprFactor implements Factor {
    private Expr base;
    private int exponent;
    private Polynomial polynomial;

    public ExprFactor(Expr base, int exponent) {
        this.base = base;
        this.exponent = exponent;
        Polynomial polynomial = Polynomial.powPolynomial(base.toPolynomial(), exponent);
        this.polynomial = polynomial;
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
