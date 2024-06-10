import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.TimableOutput;
import enums.Action;
import enums.ResetState;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class NormalElevator extends Elevator {
    private ResetState resetState;                      // 电梯的重置状态
    private final PersonQueue insidePersons;            // 电梯内部乘客, 只对 Elevator 可见
    private final PersonQueue bufferPersons;            // 候乘表缓冲区，用于暂存暂时处于 RESET 状态下分配的乘客
    private final ArrayList<Request> waitRequests;      // 请求等待队列，对 Scheduler Input Elevator 可见
    private final Schedule schedule;
    private int newCapacity;
    private int newMoveTime;
    private int transferFloor;

    public NormalElevator(int id, ArrayList<Request> waitRequests, PersonQueue outsidePersons,
                          Schedule schedule, PersonQueue bufferPersons) {
        super(id, outsidePersons);
        this.waitRequests = waitRequests;
        this.insidePersons = new PersonQueue();
        this.resetState = ResetState.NO_RESET;
        setStrategy(new NormalStrategy(this));
        this.schedule = schedule;
        this.bufferPersons = bufferPersons;
    }

    @Override
    public void run() {
        while (true) {
            Action action = nextAction();
            if (action == Action.TERMINATE) {
                return;
            } else if (action == Action.MOVE) {
                updatePosition();
            } else if (action == Action.REVERSE) {
                setDirection(getDirection() * -1);
                updatePosition();
            } else if (action == Action.OPEN) {
                open();
                out();
                in();
                close();
            } else if (action == Action.WAIT) {
                synchronized (waitRequests) {
                    waitRequests.notifyAll();
                }
                synchronized (getOutsidePersons()) {
                    if (!isOver()) {
                        try {
                            getOutsidePersons().wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (action == Action.RESET) {
                removeAllPersons();
                SingleCarElevator e1 = null;
                SingleCarElevator e2 = null;
                if (resetState == ResetState.DOUBLE_CAR_RESET) {
                    Signal signal = new Signal();
                    e1 = new SingleCarElevator(getId(), newCapacity, newMoveTime, transferFloor,
                            'A', waitRequests, signal);
                    e2 = new SingleCarElevator(getId(), newCapacity, newMoveTime, transferFloor,
                            'B', waitRequests, signal);
                    schedule.addDoubleCarElevator(e1, e2);
                }
                TimableOutput.println("RESET_BEGIN-" + getId());
                setCapacity(newCapacity);
                setMoveTime(newMoveTime);
                try { sleep(RESET_TIME); }
                catch (InterruptedException e) { throw new RuntimeException(e); }
                clearBuffer();
                TimableOutput.println("RESET_END-" + getId());
                if (updateState())  {
                    e1.setExist(true);
                    e2.setExist(true);
                    return;
                } else {
                    setExist(true);
                }
            } else {
                throw new RuntimeException("Unknown action type!");
            }
        }
    }

    private boolean updateState() {
        if (resetState == ResetState.DOUBLE_CAR_RESET) {
            synchronized (waitRequests) {
                waitRequests.notifyAll();
            }

            return true;
        } else {
            resetState = ResetState.NO_RESET;
            try {
                sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Person person;
            while ((person = bufferPersons.popPerson()) != null) {
                synchronized (getOutsidePersons()) {
                    getOutsidePersons().addPerson(person);
                    TimableOutput.println("RECEIVE-" + person.getId() + "-" + getId());
                    getOutsidePersons().notifyAll();
                }
            }
            return false;
        }
    }

    protected void in() {
        synchronized (getOutsidePersons()) {
            while (getPersonCount() < getCapacity()) {
                Person person = getOutsidePersons().popPersonFromFloor(getFloor());
                if (person == null) {
                    break;
                }
                insidePersons.addPerson(person);
                TimableOutput.println("IN-" + person.getId() + "-" + getFloor() + "-" + getId());
            }
            getOutsidePersons().notifyAll();
        }
    }

    protected void out() {
        Person person;
        while ((person = insidePersons.popPersonToFloor(getFloor())) != null) {
            TimableOutput.println("OUT-" + person.getId() + "-" + getFloor() + "-" + getId());
        }
    }

    protected void open() {
        TimableOutput.println("OPEN-" + getFloor() + "-" + getId());
        try {
            sleep(OPEN_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void close() {
        try {
            sleep(CLOSE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TimableOutput.println("CLOSE-" + getFloor() + "-" + getId());
    }

    protected void updatePosition() {
        try {
            sleep(getMoveTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setFloor(getFloor() + getDirection());
        TimableOutput.println("ARRIVE-" + getFloor() + "-" + getId());
    }

    public int getPersonCount() {
        return insidePersons.size();
    }

    public boolean hasInsideRequestsToFloor(int floor) {
        return insidePersons.hasPersonsToFloor(floor);
    }

    public boolean hasOutsideRequestsFromFloor(int floor) {
        return getOutsidePersons().hasPersonsFromFloor(floor);
    }

    public boolean hasInsideRequestsInDirection(int direction) {
        return insidePersons.hasPersonsInDirection(direction, getFloor(), true);
    }

    public boolean hasOutsideRequestsInDirection(int direction) {
        return getOutsidePersons().hasPersonsInDirection(direction, getFloor(), false);
    }

    @Override
    public boolean isEmpty() {
        return insidePersons.isEmpty() && getOutsidePersons().isEmpty() && bufferPersons.isEmpty();
    }

    public void removeAllPersons() {
        if (resetState == ResetState.NO_RESET) {
            throw new RuntimeException("Elevator-" + getId() + " should not be reset!");
        }
        if (!insidePersons.isEmpty()) {
            synchronized (waitRequests) {
                open();
                while (!insidePersons.isEmpty()) {
                    Person person = insidePersons.popPerson();
                    if (person.getToFloor() != getFloor()) {
                        waitRequests.add(new PersonRequest(
                                getFloor(), person.getToFloor(), person.getId()));
                    }
                    TimableOutput.println(
                            "OUT-" + person.getId() + "-" + getFloor() + "-" + getId());
                }
                waitRequests.notifyAll();
                close();
            }
        }
        synchronized (getOutsidePersons()) {
            while (!getOutsidePersons().isEmpty()) {
                Person person = getOutsidePersons().popPerson();
                synchronized (waitRequests) {
                    waitRequests.add(new PersonRequest(
                            person.getFromFloor(), person.getToFloor(), person.getId()));
                    waitRequests.notifyAll();
                }
            }
            getOutsidePersons().notifyAll();
        }
        clearBuffer();
    }

    private void clearBuffer() {
        synchronized (bufferPersons) {
            while (!bufferPersons.isEmpty()) {
                Person person = bufferPersons.popPerson();
                synchronized (waitRequests) {
                    waitRequests.add(new PersonRequest(
                            person.getFromFloor(), person.getToFloor(), person.getId()));
                    waitRequests.notifyAll();
                }
            }
            bufferPersons.notifyAll();
        }
    }

    public boolean isReset() {
        synchronized (getOutsidePersons()) {
            return resetState != ResetState.NO_RESET;
        }
    }

    public void normalReset(int newCapacity, int newMoveTime) {
        setExist(false);
        synchronized (getOutsidePersons()) {
            resetState = ResetState.NORMAL_RESET;
            this.newCapacity = newCapacity;
            this.newMoveTime = newMoveTime;
            getOutsidePersons().notifyAll();
        }
    }

    public void doubleCarReset(int transferFloor, int newCapacity, int newMoveTime) {
        setExist(false);
        synchronized (getOutsidePersons()) {
            resetState = ResetState.DOUBLE_CAR_RESET;
            this.newCapacity = newCapacity;
            this.newMoveTime = newMoveTime;
            this.transferFloor = transferFloor;
            getOutsidePersons().notifyAll();
        }
    }

    public ShadowNormalElevator copy() {
        return new ShadowNormalElevator(getDirection(), getFloor(), getCapacity(),
                getMoveTime(), resetState,
                (PersonQueue) insidePersons.clone(), (PersonQueue) getOutsidePersons().clone());
    }

}
