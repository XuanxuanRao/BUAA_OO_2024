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
     * @return 函数调用后的结果（一个 FuncFactor 对象）
     */
    public FuncFactor call(ArrayList<Polynomial> arguments) {
        if (arguments.size() != parameters.size()) {
            throw new IllegalArgumentException("arguments do not match parameters.");
        }
        HashMap<String, Polynomial> argumentsMap = new HashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            argumentsMap.put(parameters.get(i), arguments.get(i));
        }
        return new FuncFactor(funcBody, argumentsMap);
    }

    public String funcBody() {
        return funcBody;
    }

}
