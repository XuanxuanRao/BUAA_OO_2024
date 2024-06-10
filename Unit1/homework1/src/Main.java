import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String expression = sc.nextLine();
        Lexer lexer = new Lexer(ExprHandler.RemoveForeheadZero(ExprHandler.RemoveExtraPlusMinus(
                ExprHandler.RemoveBlank(expression))));
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        System.out.println(
                ExprHandler.adjustMinus(ExprHandler.RemoveExtraPlusMinus(expr.toString())));
    }
}
