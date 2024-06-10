public class DeriveFactor implements Factor {
    private final Polynomial polynomial;

    public DeriveFactor(Expr expr) {
        polynomial = expr.toPolynomial().derive();
    }

    @Override
    public Polynomial toPolynomial() {
        return polynomial;
    }
}
