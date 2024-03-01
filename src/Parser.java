import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final Lexer lexer;
    private final Pattern varPattern = Pattern.compile("x\\^(\\d+)");

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        int sign = 1;
        if (lexer.peek().equals("+")) {
            lexer.next();
        } else if (lexer.peek().equals("-")) {
            sign = -1;
            lexer.next();
        }
        expr.addTerm(parseTerm(sign));
        while (!lexer.isEmpty() && (lexer.peek().equals("+") || lexer.peek().equals("-"))) {
            sign = lexer.peek().equals("+") ? 1 : -1;
            lexer.next();
            expr.addTerm(parseTerm(sign));
        }
        return expr;
    }

    private Term parseTerm(int sign) {
        Term term = new Term();
        term.addFactor(parseFactor());
        while (!lexer.isEmpty() && lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        term.setSign(sign);
        return term;
    }

    private Factor parseFactor() {
        if (lexer.peek().equals("(")) {
            return parseExprFactor();
        } else if (lexer.peek().charAt(0) == 'x') {
            return parseVar();
        } else if (Character.isDigit(lexer.peek().charAt(0))) {
            return parseNumber(1);
        } else if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            if (lexer.peek().equals("+")) {
                lexer.next();
                return parseNumber(1);
            } else {
                lexer.next();
                return parseNumber(-1);
            }
        } else {
            System.out.println("Error happened when parsing factor: " + lexer.peek());
            System.exit(-1);
            return null;
        }
    }

    private ExprFactor parseExprFactor() {
        lexer.next();       // (
        Expr base = parseExpr();
        lexer.next();       // )
        ExprFactor res;
        if (!lexer.isEmpty() && lexer.peek().charAt(0) == '^') {
            res = new ExprFactor(base, Integer.parseInt(lexer.peek().substring(1)));
            lexer.next();
        } else {
            res = new ExprFactor(base, 1);
        }
        return res;
    }

    private Var parseVar() {
        Matcher matcher = varPattern.matcher(lexer.peek());
        lexer.next();
        if (matcher.find()) {
            return new Var(Integer.parseInt(matcher.group(1)));
        } else {
            return new Var(1);
        }
    }

    private Number parseNumber(int sign) {
        String number = lexer.peek();
        lexer.next();
        return new Number(number, sign);
    }

}
