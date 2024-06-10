import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.oocourse.elevator1.TimableOutput;

public class Elevator implements Runnable {
    private final int id;
    private int direction;
    private int floor;
    private boolean isOver;
    private final ArrayList<Request> insideRequests;    // 电梯内部乘客, 只对 Elevator 可见，线程安全
    private final WaitQueue outsideRequests;            // 已被分配要上这部电梯的乘客，对 Elevator 和 Scheduler 可见
    private final Strategy strategy;                    // 电梯的运行策略（捎带）
    public static final int MAX_PERSON_COUNT = 6;
    public static final int MOVE_TIME = 400;
    public static final int OPEN_TIME = 200;
    public static final int CLOSE_TIME = 200;
    public static final int ELEVATOR_NUMBER = 6;
    public static final int UP = 1;
    public static final int DOWN = -1;
    public static final int KEEP = 0;

    public Elevator(int id) {
        this.id = id;
        this.floor = 1;
        this.isOver = false;
        this.direction = UP;
        this.insideRequests = new ArrayList<>();
        this.outsideRequests = new WaitQueue();
        this.strategy = new VerticalStrategy(this);
    }

    @Override
    public void run() {
        while (true) {
            if (insideRequests.isEmpty() && outsideRequests.isEmpty() && outsideRequests.isOver()) {
                return;
            }
            Action action = strategy.nextAction();
            if (action == Action.MOVE) {
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
                synchronized (outsideRequests) {
                    if (!isOver) {
                        try {
                            outsideRequests.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    outsideRequests.notifyAll();
                }
            } else {
                TimableOutput.println("STRANGE!!!");
            }
        }
    }

    public void in() {
        while (getPersonCount() < MAX_PERSON_COUNT) {
            Request request = outsideRequests.popRequest(floor);
            if (request == null) {
                break;
            }
            insideRequests.add(request);
            TimableOutput.println("IN-" + request.getPersonId() + "-" + floor + "-" + id);
        }
    }

    public void out() {
        Iterator<Request> iterator = insideRequests.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();
            if (request.getToFloor() == floor) {
                iterator.remove();
                TimableOutput.println("OUT-" + request.getPersonId() + "-" + floor + "-" + id);
            }
        }
    }

    public void open() {
        TimableOutput.println("OPEN-" + floor + "-" + id);
        try {
            Thread.sleep(OPEN_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            Thread.sleep(CLOSE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TimableOutput.println("CLOSE-" + floor + "-" + id);
    }

    private void updatePosition() {
        try {
            Thread.sleep(MOVE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        floor += direction;
        TimableOutput.println("ARRIVE-" + floor + "-" + id);
    }

    public void addRequest(Request request) {
        outsideRequests.addRequest(request);
    }

    public int getId() {
        return id;
    }

    public int getPersonCount() {
        return insideRequests.size();
    }

    public int getFloor() {
        return floor;
    }
    
    public int getDirection() {
        return direction;
    }

    public boolean hasInsideRequests() {
        return !insideRequests.isEmpty();
    }

    public boolean hasOutsideRequests() {
        return !outsideRequests.isEmpty();
    }

    public boolean hasInsideRequestsFromFloor(int floor) {
        for (Request request : insideRequests) {
            if (request.getFromFloor() == floor) {
                return true;
            }
        }
        return false;
    }

    public boolean hasInsideRequestsToFloor(int floor) {
        for (Request request : insideRequests) {
            if (request.getToFloor() == floor) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOutsideRequestsFromFloor(int floor) {
        return outsideRequests.hasRequestsFromFloor(floor);
    }

    public boolean hasOutsideRequestsToFloor(int floor) {
        return outsideRequests.hasRequestsToFloor(floor);
    }

    public boolean hasInsideRequestsInDirection(int direction) {
        for (Request request : insideRequests) {
            if (direction > 0 ? request.getToFloor() > floor : request.getToFloor() < floor) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOutsideRequestsInDirection(int direction) {
        return outsideRequests.hasRequestsInDirection(direction, floor, false);
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver() {
        isOver = true;
        outsideRequests.setOver();
    }
}
