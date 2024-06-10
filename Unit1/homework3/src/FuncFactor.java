import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncFactor implements Factor {
    private final Polynomial polynomial;

    public FuncFactor(
            String funcBody, HashMap<String, Polynomial> arguments) {
        final String[] result = {"(" + funcBody + ")"};
        // replace x first
        if (arguments.containsKey("x")) {
            Pattern pattern = Pattern.compile("\\b(x)\\b");
            Matcher matcher = pattern.matcher(result[0]);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "(" + arguments.get("x").toString() + ")");
            }
            matcher.appendTail(sb);
            result[0] = sb.toString();
        }
        arguments.forEach((parameter, argument) -> {
            if (!parameter.equals("x")) {
                result[0] = result[0].replaceAll(parameter,
                        "(" + arguments.get(parameter).toString() + ")");
            }
        });
        Expr expr = new Parser(new Lexer(ExprHandler.RemoveExtraPlusMinus(result[0]))).parseExpr();
        polynomial = expr.toPolynomial();
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
