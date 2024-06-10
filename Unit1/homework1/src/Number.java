import java.math.BigInteger;

public class Number implements Factor {
    private BigInteger val;

    public Number(BigInteger val) {
        this.val = new BigInteger(val.toString());
    }

    public Number(String val) {
        this.val = new BigInteger(val);
    }

    public Number(String val, int sign) {
        if (sign == 1) {
            this.val = new BigInteger(val);
        } else {
            this.val = new BigInteger(val).negate();
        }
    }

    @Override
    public Polynomial toPolynomial() {
        return new Polynomial(new Monomial(val, 0));
    }

    @Override
    public String toString() {
        return val.toString();
    }

}
