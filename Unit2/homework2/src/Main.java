import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        ArrayList<Request> waitRequest = new ArrayList<>();
        Input input = new Input(waitRequest);
        new Thread(input, "Input").start();
    }
}