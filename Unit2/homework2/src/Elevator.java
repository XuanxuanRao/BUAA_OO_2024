import java.util.ArrayList;

import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.TimableOutput;
import enums.Action;


import static java.lang.Thread.sleep;

public class Elevator implements Runnable {
    private final int id;
    private int direction;
    private int floor;
    private boolean isOver;                             // 电梯是否可以结束运行
    private boolean isReset;                            // 电梯是否要重置或者正在重置
    private final PersonQueue insidePersons;            // 电梯内部乘客, 只对 Elevator 可见，线程安全
    private final PersonQueue outsidePersons;           // 已被分配要上这部电梯的乘客，对 Elevator Scheduler 可见
    private final Strategy strategy;                    // 电梯的运行策略（捎带）
    private final ArrayList<Request> waitRequests;      // 请求等待队列，对 Scheduler Input Elevator 可见
    private int capacity;                               // 电梯容量，默认为 6，可能被 RESET 指令修改
    private int moveTime;                               // 电梯移动时间，默认为 400，可能被 RESET 指令修改
    public static final int OPEN_TIME = 200;            // 电梯开关门时间，不可修改
    public static final int CLOSE_TIME = 200;           // 电梯开关门时间，不可修改
    public static final int RESET_TIME =  1200;         // 电梯重置时间，不可修改
    private int newCapacity;
    private int newMoveTime;

    public Elevator(int id, ArrayList<Request> waitRequests, PersonQueue outsidePersons) {
        this.id = id;
        this.waitRequests = waitRequests;
        this.insidePersons = new PersonQueue();
        this.outsidePersons = outsidePersons;
        this.strategy = new Strategy(this);
        this.floor = 1;
        this.capacity = 6;
        this.direction = 1;
        this.moveTime = 400;
        this.isReset = false;
        this.isOver = false;
    }

    @Override
    public void run() {
        while (true) {
            Action action = strategy.nextAction();
            if (action == Action.TERMINATE) {
                return;
            } else if (action == Action.MOVE) {
                updatePosition();
            } else if (action == Action.REVERSE) {
                direction = -direction;
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
                synchronized (outsidePersons) {
                    if (!isOver) {
                        try {
                            outsidePersons.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (action == Action.RESET) {
                removeAllPersons();
                TimableOutput.println("RESET_BEGIN-" + id);
                capacity = newCapacity;
                moveTime = newMoveTime;
                try {
                    sleep(RESET_TIME);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                TimableOutput.println("RESET_END-" + id);
                isReset = false;
            } else {
                throw new RuntimeException("Unknown action type!");
            }
        }
    }

    private void in() {
        synchronized (outsidePersons) {
            while (getPersonCount() < capacity) {
                Person person = outsidePersons.popPersonFromFloor(floor);
                if (person == null) {
                    break;
                }
                insidePersons.addPerson(person);
                TimableOutput.println("IN-" + person.getId() + "-" + floor + "-" + id);
            }
            outsidePersons.notifyAll();
        }
    }

    private void out() {
        Person person;
        while ((person = insidePersons.popPersonToFloor(floor)) != null) {
            TimableOutput.println("OUT-" + person.getId() + "-" + floor + "-" + id);
        }
    }

    private void open() {
        TimableOutput.println("OPEN-" + floor + "-" + id);
        try {
            sleep(OPEN_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            sleep(CLOSE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TimableOutput.println("CLOSE-" + floor + "-" + id);
    }

    private void updatePosition() {
        try {
            sleep(moveTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        floor += direction;
        TimableOutput.println("ARRIVE-" + floor + "-" + id);
    }

    public int getId() {
        return id;
    }

    public int getPersonCount() {
        return insidePersons.size();
    }

    public int getFloor() {
        return floor;
    }

    public int getDirection() {
        return direction;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean hasInsideRequests() {
        return !insidePersons.isEmpty();
    }

    public boolean hasOutsideRequests() {
        return !outsidePersons.isEmpty();
    }

    public boolean hasInsideRequestsToFloor(int floor) {
        return insidePersons.haspPersonsToFloor(floor);
    }

    public boolean hasOutsideRequestsFromFloor(int floor) {
        return outsidePersons.hasPersonsFromFloor(floor);
    }

    public boolean hasInsideRequestsInDirection(int direction) {
        return insidePersons.hasPersonsInDirection(direction, floor, true);
    }

    public boolean hasOutsideRequestsInDirection(int direction) {
        return outsidePersons.hasPersonsInDirection(direction, floor, false);
    }

    public void removeAllPersons() {
        if (!isReset) {
            throw new RuntimeException("Elevator-" + id + " should not be reset!");
        }
        if (!insidePersons.isEmpty()) {
            synchronized (waitRequests) {
                open();
                while (!insidePersons.isEmpty()) {
                    Person person = insidePersons.popPerson();
                    if (person.getToFloor() != floor) {
                        waitRequests.add(new PersonRequest(
                                floor, person.getToFloor(), person.getId()));
                    }
                    TimableOutput.println("OUT-" + person.getId() + "-" + floor + "-" + id);
                }
                waitRequests.notifyAll();
                close();
            }
        }
        synchronized (outsidePersons) {
            while (!outsidePersons.isEmpty()) {
                Person person = outsidePersons.popPerson();
                synchronized (waitRequests) {
                    waitRequests.add(new PersonRequest(
                            person.getFromFloor(), person.getToFloor(), person.getId()));
                    waitRequests.notifyAll();
                }
            }
            outsidePersons.notifyAll();
        }
    }

    public boolean isOver() {
        synchronized (outsidePersons) {
            return isOver;
        }
    }

    public void setOver() {
        synchronized (outsidePersons) {
            isOver = true;
            outsidePersons.notifyAll();
        }
    }

    public boolean isReset() {
        synchronized (outsidePersons) {
            return isReset;
        }
    }

    public void setReset(int newCapacity, int newMoveTime) {
        synchronized (outsidePersons) {
            isReset = true;
            this.newCapacity = newCapacity;
            this.newMoveTime = newMoveTime;
            outsidePersons.notifyAll();
        }
    }

    public ShadowElevator copy() {
        return new ShadowElevator(id, direction, floor, capacity, moveTime,
                (PersonQueue) insidePersons.clone(), (PersonQueue) outsidePersons.clone());
    }

}
