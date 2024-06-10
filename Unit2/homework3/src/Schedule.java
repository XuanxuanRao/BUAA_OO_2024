import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.TimableOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class Schedule implements Runnable {
    private final ArrayList<Request> waitRequests;
    private final CopyOnWriteArrayList<Elevator> elevators = new CopyOnWriteArrayList<>();
    private final ArrayList<PersonQueue> allBufferPersons = new ArrayList<>();
    private boolean inputOver;
    private boolean isOver = false;
    private static final int ELEVATOR_NUMBER = 6;

    public Schedule(ArrayList<Request> requests) {
        this.waitRequests = requests;
        for (int i = 0; i < ELEVATOR_NUMBER; i++) {
            allBufferPersons.add(new PersonQueue());
            elevators.add(new NormalElevator(i + 1, waitRequests, new PersonQueue(), this,
                    allBufferPersons.get(i)));
        }
        this.inputOver = false;
        for (int i = 0; i < ELEVATOR_NUMBER; i++) {
            new Thread(elevators.get(i), "NormalElevator-" + elevators.get(i).getId()).start();
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (waitRequests) {
                if (isOver()) {
                    isOver = true;
                    elevators.forEach(Elevator::setOver);
                    waitRequests.notifyAll();
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
                handlePersonRequest((PersonRequest) request);
            } else if (request instanceof NormalResetRequest) {
                handleNormalResetRequest((NormalResetRequest) request);
            } else if (request instanceof DoubleCarResetRequest) {
                handleDoubleCarResetRequest((DoubleCarResetRequest) request);
            } else {
                throw new RuntimeException("Unknown request type!");
            }
        }
    }

    private void handleDoubleCarResetRequest(DoubleCarResetRequest request) {
        int elevatorId = request.getElevatorId();
        NormalElevator normalElevator = (NormalElevator) getElevator(elevatorId);
        normalElevator.doubleCarReset(request.getTransferFloor(),
                request.getCapacity(), (int) (request.getSpeed() * 1000));
    }

    private void handleNormalResetRequest(NormalResetRequest request) {
        int elevatorId = request.getElevatorId();
        ((NormalElevator) getElevator(elevatorId)).normalReset(
                request.getCapacity(), (int) (request.getSpeed() * 1000));
    }

    private void handlePersonRequest(PersonRequest request) {
        Person person = new Person(request.getPersonId(),
                request.getFromFloor(), request.getToFloor());
        Elevator elevator = choose(person);
        if (elevator instanceof NormalElevator && ((NormalElevator) elevator).isReset()) {
            synchronized (allBufferPersons.get(elevator.getId() - 1)) {
                allBufferPersons.get(elevator.getId() - 1).addPerson(person);
                allBufferPersons.get(elevator.getId() - 1).notifyAll();
            }
        } else {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized (elevator.getOutsidePersons()) {
                elevator.getOutsidePersons().addPerson(person);
                elevator.getOutsidePersons().notifyAll();
                TimableOutput.println("RECEIVE-" + person.getId() + "-" + elevator.getId() +
                        (elevator instanceof SingleCarElevator ?
                                "-" + ((SingleCarElevator) elevator).getKind() : ""));
            }
        }
    }

    public void addDoubleCarElevator(SingleCarElevator elevator1, SingleCarElevator elevator2) {
        if (isOver) {
            elevator1.setOver();
            elevator2.setOver();
        }
        elevators.add(elevator1);
        elevators.add(elevator2);
        new Thread(elevator1).start();
        new Thread(elevator2).start();
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
            Iterator<Request> iterator = waitRequests.iterator();
            while (iterator.hasNext()) {
                Request request = iterator.next();
                if (request instanceof NormalResetRequest
                        || request instanceof DoubleCarResetRequest) {
                    iterator.remove();
                    return request;
                }
            }
            return waitRequests.remove(0);
        }
    }

    private Elevator choose(Person person) {
        // this seems to be stupid but can save me from being fucked violently in mutual
        while (getElevatorNumber() <= 2) {
            try {
                sleep(1200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Elevator target = null;
        int minCost = Integer.MAX_VALUE;
        for (Elevator elevator : elevators) {
            if (!elevator.exist()) { continue; }
            if (elevator instanceof SingleCarElevator) {
                if (((SingleCarElevator) elevator).canAccept(person)) {
                    int cost = ((SingleCarElevator) elevator).copy().stimulate(person);
                    if (!((SingleCarElevator) elevator).canFinish(person)) {
                        SingleCarElevator tmp = getAnotherElevator((SingleCarElevator) elevator);
                        cost += tmp.copy().stimulate(new Person(
                                person.getId(), tmp.getTransferFloor(), person.getToFloor()));
                    }
                    if (cost < minCost) {
                        minCost = cost;
                        target = elevator;
                    }
                }
            } else {
                int cost = ((NormalElevator) elevator).copy().stimulate(person);
                if (cost < minCost) {
                    minCost = cost;
                    target = elevator;
                }
            }
        }
        return target;
    }

    private boolean isOver() {
        synchronized (waitRequests) {
            if (!inputOver || !waitRequests.isEmpty()) {
                return false;
            }
        }
        for (Elevator elevator : elevators) {
            synchronized (elevator.getOutsidePersons()) {
                if (!elevator.isEmpty()) {
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

    private Elevator getElevator(int id) {
        return elevators.stream()
                .filter(elevator -> elevator.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private SingleCarElevator getAnotherElevator(SingleCarElevator singleCarElevator) {
        return (SingleCarElevator) elevators.stream()
                .filter(elevator -> elevator instanceof SingleCarElevator &&
                        elevator.getId() == singleCarElevator.getId() &&
                        ((SingleCarElevator) elevator).getKind() != singleCarElevator.getKind())
                .findFirst()
                .orElse(null);
    }

    private int getElevatorNumber() {
        int cnt = 0;
        for (Elevator elevator : elevators) {
            cnt += elevator.exist() ? 1 : 0;
        }
        return cnt;
    }
}
