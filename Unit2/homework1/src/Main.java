import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        WaitQueue waitRequests = new WaitQueue();
        Thread inputThread = new Thread(new Input(waitRequests), "Input");
        ArrayList<Elevator> elevators = new ArrayList<>();
        for (int i = 0; i < Elevator.ELEVATOR_NUMBER; i++) {
            elevators.add(new Elevator(i + 1));
        }
        Thread scheduleThread = new Thread(new Schedule(waitRequests, elevators), "Schedule");
        inputThread.start();
        scheduleThread.start();
        for (Elevator elevator : elevators) {
            Thread elevatorThread = new Thread(elevator, "Elevator-" + elevator.getId());
            elevatorThread.start();
        }
    }
}