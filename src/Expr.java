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
        Polynomial polynomial = new Polynomial();
        for (Term term : terms) {
            if (term.getSign() == 1) {
                polynomial.addPolynomial(term.toPolynomial());
            } else {
                polynomial.subPolynomial(term.toPolynomial());
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
