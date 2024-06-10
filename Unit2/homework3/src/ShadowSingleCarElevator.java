public class ShadowSingleCarElevator {
    private int direction;
    private int floor;
    private final int transferFloor;
    private final char kind;
    private final PersonQueue insidePersons;
    private final PersonQueue outsidePersons;
    private final int capacity;
    private static final int OPEN_COST = 225;
    private static final int CLOSE_COST = 225;
    private final int moveCost;

    public ShadowSingleCarElevator(int direction, int floor, int transferFloor,
                                   int capacity, int moveTime, char kind,
                                   PersonQueue insidePersons, PersonQueue outsidePersons) {
        this.direction = direction;
        this.floor = floor;
        this.transferFloor = transferFloor;
        this.capacity = capacity;
        this.kind = kind;
        this.insidePersons = insidePersons;
        this.outsidePersons = outsidePersons;
        this.moveCost = 100 + moveTime;
    }

    int stimulate(Person person) {
        int cost = 0;
        outsidePersons.addPerson(person);
        while (!insidePersons.isEmpty() || !outsidePersons.isEmpty()) {
            if (canOpen()) {
                out();
                in();
                cost += OPEN_COST + CLOSE_COST;
            } else if (canMove()) {
                floor += direction;
                cost += moveCost;
            } else {
                direction *= -1;
                floor += direction;
                cost += moveCost;
            }
        }
        return cost;
    }

    private boolean canOpen() {
        if (insidePersons.hasPersonsToFloor(floor) || ((floor == transferFloor) &&
                (kind == 'A' ? insidePersons.hasPersonsToTransfer(1, transferFloor) :
                insidePersons.hasPersonsToTransfer(transferFloor, 11)))) {
            return true;
        }
        if (insidePersons.size() == capacity) {
            return false;
        }
        return outsidePersons.hasPersonsFromFloor(floor);
    }

    private boolean canMove() {
        if (kind == 'A' && floor + direction > transferFloor) {
            return false;
        } else if (kind == 'B' && floor + direction < transferFloor) {
            return false;
        }
        return insidePersons.hasPersonsInDirection(direction, floor, true) ||
                outsidePersons.hasPersonsInDirection(direction, floor, false);
    }

    private void out() {
        while (insidePersons.popPersonToFloor(floor) != null) {}
        if (floor != transferFloor) {
            return;
        }
        while (insidePersons.popPersonToTransfer(
                kind == 'A' ? 1 : transferFloor, kind == 'A' ? transferFloor : 11) != null) {}
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
