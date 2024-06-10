import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FuncManager {
    private static final HashMap<String, FuncDefine> funcDefines = new HashMap<>();

    private static void add(String funcName, FuncDefine funcDefine) {
        funcDefines.put(funcName, funcDefine);
    }

    public static FuncDefine get(String funcName) {
        return funcDefines.get(funcName);
    }

    /**
     * Parse the definition of function(String) to FuncDefine and add it to funcDefines(map)
     * @param input Function definition
     */
    public static void recordFunc(String input) {
        Pattern funcPattern = Pattern.compile("(\\w+)\\(([^=]+)\\)=(.*)");
        Matcher matcher = funcPattern.matcher(input);
        if (matcher.find()) {
            String funcName = matcher.group(1);
            String[] parameters = matcher.group(2).split(",");
            String funcBody = matcher.group(3);
            add(funcName, new FuncDefine(funcBody, new ArrayList<>(Arrays.asList(parameters))));
        } else {
            System.out.println("Error: Invalid function definition");
            System.exit(-1);
        }
    }

}
