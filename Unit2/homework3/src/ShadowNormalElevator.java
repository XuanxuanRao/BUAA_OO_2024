import enums.ResetState;

public class ShadowNormalElevator {
    private int direction;
    private int floor;
    private final ResetState resetState;
    private final PersonQueue insidePersons;            // 电梯内部乘客
    private final PersonQueue outsidePersons;           // 已被分配要上这部电梯的乘客
    private final int capacity;                         // 电梯容量
    private final int moveTime;                         // 电梯移动时间
    private static final int OPEN_COST = 300;           // 电梯开门系统开销
    private static final int CLOSE_COST = 300;          // 电梯关门系统开销

    public ShadowNormalElevator(int direction, int floor, int capacity,
                                int moveTime, ResetState resetState,
                                PersonQueue insidePersons, PersonQueue outsidePersons) {
        this.floor = floor;
        this.capacity = capacity;
        this.direction = direction;
        this.moveTime = moveTime;
        this.resetState = resetState;
        this.insidePersons = insidePersons;
        this.outsidePersons = outsidePersons;
    }

    int stimulate(Person person) {
        int cost = 0;
        if (resetState == ResetState.NORMAL_RESET) {
            if (!insidePersons.isEmpty()) {
                cost += OPEN_COST + CLOSE_COST + Elevator.RESET_TIME;
                insidePersons.clear();
                outsidePersons.clear();
            }
        }
        outsidePersons.addPerson(person);
        while (!insidePersons.isEmpty() || !outsidePersons.isEmpty()) {
            if (canOpen()) {
                out();
                in();
                cost += OPEN_COST + CLOSE_COST;
            } else if (insidePersons.hasPersonsInDirection(direction, floor, true) ||
                    outsidePersons.hasPersonsInDirection(direction, floor, false)) {
                floor += direction;
                cost += 400 + moveTime;
            } else {
                direction *= -1;
                floor += direction;
                cost += 400 + moveTime;
            }
        }
        return cost;
    }

    private boolean canOpen() {
        // If a person needs to get off
        if (insidePersons.hasPersonsToFloor(floor)) {
            return true;
        }
        // If the elevator is full
        if (insidePersons.size() == capacity) {
            return false;
        }
        // If a person is to get on
        return outsidePersons.hasPersonsFromFloor(floor);
    }

    private void out() {
        while (insidePersons.popPersonToFloor(floor) != null) {}
    }

    private void in() {
        while (insidePersons.size() < capacity) {
            Person person = outsidePersons.popPersonFromFloor(floor);
            if (person == null) {
                break;
            }
            insidePersons.addPerson(person);
        }
    }

}
