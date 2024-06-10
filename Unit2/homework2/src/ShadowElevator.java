public class ShadowElevator {
    private final int id;
    private int direction;
    private int floor;
    private final PersonQueue insidePersons;            // 电梯内部乘客
    private final PersonQueue outsidePersons;           // 已被分配要上这部电梯的乘客
    private final int capacity;                         // 电梯容量
    private final int moveTime;                         // 电梯移动时间
    private static final int OPEN_COST = 200;           // 电梯开门耗电量
    private static final int CLOSE_COST = 200;          // 电梯关门耗电量
    private static final int MOVE_COST = 400;           // 电梯移动耗电量

    public ShadowElevator(int id, int direction, int floor, int capacity, int moveTime,
                          PersonQueue insidePersons, PersonQueue outsidePersons) {
        this.id = id;
        this.floor = floor;
        this.capacity = capacity;
        this.direction = direction;
        this.moveTime = moveTime;
        this.insidePersons = insidePersons;
        this.outsidePersons = outsidePersons;
    }

    int stimulate(Person person) {
        int time = 0;
        int power = 0;
        outsidePersons.addPerson(person);
        while (!insidePersons.isEmpty() || !outsidePersons.isEmpty()) {
            if (canOpen()) {
                out();
                in();
                time += Elevator.OPEN_TIME + Elevator.CLOSE_TIME;
                power += OPEN_COST + CLOSE_COST;
            } else if (insidePersons.hasPersonsInDirection(direction, floor, true) ||
                    outsidePersons.hasPersonsInDirection(direction, floor, false)) {
                floor += direction;
                time += moveTime;
                power += MOVE_COST;
            } else {
                direction *= -1;
                floor += direction;
                time += moveTime;
            }
        }
        return time + power;
    }

    private boolean canOpen() {
        // If a person needs to get off
        if (insidePersons.haspPersonsToFloor(floor)) {
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
        while (insidePersons.popPersonToFloor(floor) != null) {
        }
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
