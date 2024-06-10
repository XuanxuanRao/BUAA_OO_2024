import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.TimableOutput;
import enums.Action;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class SingleCarElevator extends Elevator {
    private final int transferFloor;                // 换乘楼层
    private final char kind;                        // 轿厢种类
    private final PersonQueue insidePersons;
    private final ArrayList<Request> waitRequests;  // 请求等待队列，对 Scheduler Input Elevator 可见
    private final Signal occupiedSignal;

    public SingleCarElevator(int id, int capacity, int moveTime, int transferFloor, char kind,
                             ArrayList<Request> waitRequests, Signal occupiedSignal) {
        super(id, kind == 'A' ? transferFloor - 1 : transferFloor + 1, kind == 'A' ? 1 : -1,
                capacity, moveTime);
        this.insidePersons = new PersonQueue();
        this.waitRequests = waitRequests;
        this.kind = kind;
        this.transferFloor = transferFloor;
        this.occupiedSignal = occupiedSignal;
        setExist(false);
        setStrategy(new SingleCarStrategy(this));
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
            } else {
                throw new RuntimeException("Unknown action type!");
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return insidePersons.isEmpty() && getOutsidePersons().isEmpty();
    }

    @Override
    protected void updatePosition() {
        try {
            sleep(getMoveTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setFloor(getFloor() + getDirection());
        if (getFloor() == transferFloor) {
            occupiedSignal.setOccupied();
        }
        TimableOutput.println("ARRIVE-" + getFloor() + "-" + getId() + "-" + kind);
        if (getFloor() - getDirection() == transferFloor) {
            occupiedSignal.setFree();
        }
    }

    @Override
    protected void open() {
        TimableOutput.println("OPEN-" + getFloor() + "-" + getId() + "-" + kind);
        try {
            sleep(OPEN_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void close() {
        try {
            sleep(CLOSE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TimableOutput.println("CLOSE-" + getFloor() + "-" + getId() + "-" + kind);
    }

    @Override
    protected void in() {
        synchronized (getOutsidePersons()) {
            while (getPersonCount() < getCapacity()) {
                Person person = getOutsidePersons().popPersonFromFloor(getFloor());
                if (person == null) {
                    break;
                }
                insidePersons.addPerson(person);
                TimableOutput.println(
                        "IN-" + person.getId() + "-" + getFloor() + "-" + getId() + "-" + kind);
            }
            getOutsidePersons().notifyAll();
        }
    }

    @Override
    protected void out() {
        Person person;
        while ((person = insidePersons.popPersonToFloor(getFloor())) != null) {
            TimableOutput.println(
                    "OUT-" + person.getId() + "-" + getFloor() + "-" + getId() + "-" + kind);
        }
        if (getFloor() != transferFloor) {
            return;
        }
        while ((person = insidePersons.popPersonToTransfer(
                kind == 'A' ? 1 : transferFloor, kind == 'A' ? transferFloor : 11))
                != null) {
            TimableOutput.println(
                    "OUT-" + person.getId() + "-" + getFloor() + "-" + getId() + "-" + kind);
            synchronized (waitRequests) {
                waitRequests.add(
                        new PersonRequest(getFloor(), person.getToFloor(), person.getId()));
                waitRequests.notifyAll();
            }
        }
    }

    public int getPersonCount() {
        return insidePersons.size();
    }

    public boolean hasInsidePersonsToFloor(int floor) {
        return insidePersons.hasPersonsToFloor(floor);
    }

    public boolean hasOutsidePersonsFromFloor(int floor) {
        return getOutsidePersons().hasPersonsFromFloor(floor);
    }

    public boolean hasInsidePersonsInDirection(int direction) {
        return insidePersons.hasPersonsInDirection(direction, getFloor(), true);
    }

    public boolean hasOutsidePersonsInDirection(int direction) {
        return getOutsidePersons().hasPersonsInDirection(direction, getFloor(), false);
    }

    public boolean hasInsidePersonsToTransfer() {
        return kind == 'A' ? insidePersons.hasPersonsToTransfer(1, transferFloor) :
                insidePersons.hasPersonsToTransfer(transferFloor, 11);
    }

    public int getTransferFloor() {
        return transferFloor;
    }

    public char getKind() {
        return kind;
    }

    public boolean canAccept(Person person) {
        if (kind == 'A') {
            return person.getFromFloor() < transferFloor ||
                    (person.getFromFloor() == transferFloor && person.getToFloor() < transferFloor);
        } else {
            return person.getFromFloor() > transferFloor ||
                    (person.getFromFloor() == transferFloor && person.getToFloor() > transferFloor);
        }
    }

    public boolean canFinish(Person person) {
        return kind == 'A' ?
                person.getFromFloor() <= transferFloor && person.getToFloor() <= transferFloor :
                person.getFromFloor() >= transferFloor && person.getToFloor() >= transferFloor;
    }

    public ShadowSingleCarElevator copy() {
        return new ShadowSingleCarElevator(getDirection(), getFloor(), transferFloor,
                getCapacity(), getMoveTime(), kind,
                (PersonQueue) insidePersons.clone(), (PersonQueue) getOutsidePersons().clone());
    }

}
