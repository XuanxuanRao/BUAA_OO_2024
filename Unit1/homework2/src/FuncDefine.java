import java.util.ArrayList;
import java.util.HashMap;

public class FuncDefine {

    private final String funcBody;
    private final ArrayList<String> parameters;

    public FuncDefine(String funcBody, ArrayList<String> parameters) {
        this.funcBody = funcBody;
        this.parameters = new ArrayList<>(parameters);
    }

    /**
     * Return the mapping relationship between parameters and arguments as a hashmap
     *
     * @param arguments 函数调用的实参
     * @return 函数形参与实参的映射关系
     */
    public HashMap<String, Polynomial> call(ArrayList<Polynomial> arguments) {
        if (arguments.size() != parameters.size()) {
            System.out.println(funcBody);
            for (Polynomial argument : arguments) {
                System.out.println(argument);
            }
            throw new IllegalArgumentException("arguments do not match parameters.");
        }
        HashMap<String, Polynomial> argumentsMap = new HashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            argumentsMap.put(parameters.get(i), arguments.get(i));
        }
        return argumentsMap;
    }

    public String funcBody() {
        return funcBody;
    }

}
