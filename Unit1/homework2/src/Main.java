import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.nextLine();
        while (n-- > 0) {
            String func = sc.nextLine();
            FuncManager.recordFunc(ExprHandler.RemoveForeheadZero(ExprHandler.RemoveExtraPlusMinus(
                    ExprHandler.RemoveBlank(func))));
        }
        String expression = sc.nextLine();
        Lexer lexer = new Lexer(ExprHandler.RemoveForeheadZero(ExprHandler.RemoveExtraPlusMinus(
                ExprHandler.RemoveBlank(expression))));
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        System.out.println(ExprHandler.RemoveExtraPlusMinus(expr.toString()));
    }
}
