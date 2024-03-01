import java.util.ArrayList;

public class Lexer {
    private ArrayList<String> tokens;
    private final char[] operators = {'+', '-', '*', '(', ')'};
    private int pos;

    public Lexer(String input) {
        int pos = 0;
        tokens = new ArrayList<>();
        while (pos < input.length()) {
            char ch = input.charAt(pos);
            if (Character.isDigit(ch)) {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                    sb.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(sb.toString());
            } else if (ch == 'x') {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length() && !isOperator(input.charAt(pos))) {
                    sb.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(sb.toString());
            } else if (isOperator(ch)) {
                tokens.add(String.valueOf(ch));
                pos++;
            } else if (ch == '^') {
                pos++;
                StringBuilder sb = new StringBuilder();
                sb.append('^');
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                    sb.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(sb.toString());
            }
        }
    }

    private boolean isOperator(char ch) {
        for (char operator : operators) {
            if (ch == operator) {
                return true;
            }
        }
        return false;
    }

    public String peek() {
        return tokens.get(pos);
    }

    public void next() {
        pos++;
    }

    public boolean isEmpty() {
        return pos == tokens.size();
    }

}
