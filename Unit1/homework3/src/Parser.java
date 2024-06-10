import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final Lexer lexer;
    private final Pattern varPattern = Pattern.compile("x\\^(\\d+)");
    private final Set<String> funcNames = new HashSet<>(Arrays.asList("f", "g", "h"));

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
        if (!lexer.isEmpty() && lexer.peek().equals(",")) {
            lexer.next();
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
            int sign = lexer.peek().equals("+") ? 1 : -1;
            lexer.next();
            return parseNumber(sign);
        } else if (funcNames.contains(lexer.peek())) {
            return parseFuncFactor();
        } else if (lexer.peek().equals("exp")) {
            return parseExpFactor();
        } else if (lexer.peek().equals("dx")) {
            return parseDeriveFactor();
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
            return new Var(matcher.group(1));
        } else {
            return new Var("1");
        }
    }

    private Number parseNumber(int sign) {
        String number = lexer.peek();
        lexer.next();
        return new Number(number, sign);
    }

    private FuncFactor parseFuncFactor() {
        final String funcName = lexer.peek();
        lexer.next();
        lexer.next();       // (
        ArrayList<Polynomial> arguments = new ArrayList<>();
        while (!lexer.isEmpty() && !lexer.peek().equals(")")) {
            arguments.add(parseExpr().toPolynomial());
        }
        lexer.next();       // )
        FuncDefine funcDefine = FuncManager.get(funcName);
        return funcDefine.call(arguments);
    }

    private ExpFactor parseExpFactor() {
        lexer.next();
        lexer.next();   // (
        Expr expr = parseExpr();
        lexer.next();   // )
        ExpFactor res;
        if (!lexer.isEmpty() && lexer.peek().charAt(0) == '^') {
            String exponent = lexer.peek().substring(1);
            lexer.next();
            res = new ExpFactor(expr.toPolynomial().mulMonomial(new Monomial(exponent, Unit.ONE)));
        } else {
            res = new ExpFactor(expr.toPolynomial());
        }
        return res;
    }

    private DeriveFactor parseDeriveFactor() {
        lexer.next();   // dx
        lexer.next();   // (
        Expr expr = parseExpr();
        lexer.next();   // (
        return new DeriveFactor(expr);
    }

}
