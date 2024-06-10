import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ResetRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Schedule implements Runnable {
    private final ArrayList<Request> waitRequests;
    private final ArrayList<PersonQueue> waitLists = new ArrayList<>();
    private final ArrayList<Elevator> elevators = new ArrayList<>();
    private boolean inputOver;
    private static int ELEVATOR_NUMBER = 6;

    public Schedule(ArrayList<Request> requests) {
        this.waitRequests = requests;
        for (int i = 0; i < ELEVATOR_NUMBER; i++) {
            waitLists.add(new PersonQueue());
            elevators.add(new Elevator(i + 1, waitRequests, waitLists.get(i)));
        }
        this.inputOver = false;
        for (int i = 0; i < ELEVATOR_NUMBER; i++) {
            new Thread(elevators.get(i), "Elevator-" + elevators.get(i).getId()).start();
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (waitRequests) {
                if (isOver()) {
                    for (Elevator elevator : elevators) {
                        elevator.setOver();
                    }
                    waitRequests.notifyAll();
                    // TimableOutput.println("ScheduleThread-OVER");   // 每次都会顺利结束调度线程
                    return;
                }
                waitRequests.notifyAll();
            }
            Request request = getRequest();
            if (request == null) {
                synchronized (waitRequests) {
                    try {
                        waitRequests.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    waitRequests.notifyAll();
                }
            } else if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                Person person = new Person(personRequest.getPersonId(),
                        personRequest.getFromFloor(), personRequest.getToFloor());
                int elevatorId = 0;
                try {
                    elevatorId = choose(person);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                PersonQueue outsidePersons = waitLists.get(elevatorId - 1);
                synchronized (outsidePersons) {
                    outsidePersons.addPerson(person);
                    outsidePersons.notifyAll();
                }
                TimableOutput.println("RECEIVE-" + person.getId() + "-" + elevatorId);
            } else if (request instanceof ResetRequest) {
                ResetRequest resetRequest = (ResetRequest) request;
                int elevatorId = resetRequest.getElevatorId();
                PersonQueue outsidePersons = waitLists.get(elevatorId - 1);
                elevators.get(elevatorId - 1).setReset(
                        resetRequest.getCapacity(), (int) (resetRequest.getSpeed() * 1000));
            } else {
                throw new RuntimeException("Unknown request type!");
            }
        }
    }

    private Request getRequest() {
        synchronized (waitRequests) {
            while (waitRequests.isEmpty() && !inputOver) {
                try {
                    waitRequests.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (waitRequests.isEmpty()) {
                return null;
            }
            waitRequests.notifyAll();
            return waitRequests.remove(0);
        }
    }

    private int choose(Person person) throws InterruptedException {
        while (allElevatorsReset()) {
            sleep(1200);
        }
        int minCost = Integer.MAX_VALUE;
        int elevatorId = -1;
        for (int i = 0; i < ELEVATOR_NUMBER; i++) {
            if (elevators.get(i).isReset()) {
                continue;
            }
            int cost = elevators.get(i).copy().stimulate(person);
            if (cost < minCost) {
                minCost = cost;
                elevatorId = i;
            }
        }
        return elevatorId + 1;
    }

    private boolean isOver() {
        synchronized (waitRequests) {
            if (!inputOver || !waitRequests.isEmpty()) {
                return false;
            }
        }

        for (Elevator elevator : elevators) {
            if (elevator.hasInsideRequests()) {
                return false;
            }
        }

        for (PersonQueue outsidePersons : waitLists) {
            synchronized (outsidePersons) {
                if (!outsidePersons.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setInputOver() {
        synchronized (waitRequests) {
            inputOver = true;
            waitRequests.notifyAll();
        }
    }

    private boolean allElevatorsReset() {
        for (Elevator elevator : elevators) {
            if (!elevator.isReset()) {
                return false;
            }
        }
        return true;
    }

}
