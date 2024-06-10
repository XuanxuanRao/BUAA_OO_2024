import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;

public class Input implements Runnable {
    private final WaitQueue waitRequests;

    public Input(WaitQueue waitRequests) {
        this.waitRequests = waitRequests;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest personRequest = elevatorInput.nextPersonRequest();
            if (personRequest == null) {
                try {
                    elevatorInput.close();
                    waitRequests.setOver();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else {
                waitRequests.addRequest(new Request(personRequest.getPersonId(),
                        personRequest.getElevatorId(),
                        personRequest.getFromFloor(),
                        personRequest.getToFloor()));
            }
        }
    }
}
