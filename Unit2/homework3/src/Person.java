public class Person {
    private final int id;
    private final int fromFloor;
    private final int toFloor;

    public Person(int id, int fromFloor, int toFloor) {
        this.id = id;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    public int getId() {
        return id;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public boolean needTransfer(int lowerBound, int upperBound) {
        return toFloor < lowerBound || toFloor > upperBound;
    }

    @Override
    protected Person clone() {
        return new Person(this.id, this.fromFloor, this.toFloor);
    }

    @Override
    public String toString() {
        return String.format("%d-FROM-%d-TO-%d", this.id, this.fromFloor, this.toFloor);
    }
}
