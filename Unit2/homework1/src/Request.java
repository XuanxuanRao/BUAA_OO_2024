public class Request {
    private final int personId;
    private final int elevatorId;
    private final int fromFloor;
    private final int toFloor;

    public Request(int personId, int elevatorId, int fromFloor, int toFloor) {
        this.personId = personId;
        this.elevatorId = elevatorId;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    public int getPersonId() {
        return personId;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    @Override
    public String toString() {
        return personId + "-FROM-" + fromFloor + "-TO-" + toFloor;
    }
}
