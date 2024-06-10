import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.Request;

import java.util.ArrayList;

public class Input implements Runnable {
    private final ArrayList<Request> requests;
    private final Schedule schedule;

    public Input(ArrayList<Request> requests) {
        this.requests = requests;
        this.schedule = new Schedule(requests);
        new Thread(this.schedule, "Schedule").start();
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                try {
                    elevatorInput.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                schedule.setInputOver();
                return;
            } else {
                synchronized (requests) {
                    requests.add(request);
                    requests.notifyAll();
                }
            }
        }
    }

}
