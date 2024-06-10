public final class ExprHandler {
    public static String RemoveExtraPlusMinus(String expression) {
        StringBuilder sb = new StringBuilder();
        boolean isSigned = false;
        int sign = 1;
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch == '+') {
                isSigned = true;
            } else if (ch == '-') {
                sign *= -1;
                isSigned = true;
            } else {
                if (isSigned) {
                    sb.append(sign == 1 ? '+' : '-');
                }
                sb.append(ch);
                sign = 1;
                isSigned = false;
            }
        }
        return sb.toString().replaceAll("(\\^)(\\+?)", "$1");
    }

    public static String RemoveBlank(String expression) {
        return expression.replaceAll("[ \\t]", "");
    }

    public static String RemoveForeheadZero(String expression) {
        return expression.replaceAll("\\b0+(\\d+)", "$1");
    }

}
