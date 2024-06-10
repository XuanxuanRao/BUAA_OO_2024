import java.util.ArrayList;

public class Expr {
    private ArrayList<Term> terms;

    public Expr() {
        terms = new ArrayList<>();
    }

    public Expr(ArrayList<Term> terms) {
        this.terms = terms;
    }

    public Polynomial toPolynomial() {
        Polynomial polynomial = Polynomial.ZERO;
        for (Term term : terms) {
            if (term.getSign() == 1) {
                polynomial = polynomial.addPolynomial(term.toPolynomial());
            } else {
                polynomial = polynomial.subPolynomial(term.toPolynomial());
            }
        }
        return polynomial;
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    @Override
    public String toString() {
        return this.toPolynomial().toString();
    }

}
