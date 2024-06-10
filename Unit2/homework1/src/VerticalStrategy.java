public class VerticalStrategy implements Strategy {
    private final Elevator elevator;

    public VerticalStrategy(Elevator elevator) {
        this.elevator = elevator;
    }

    @Override
    public Action nextAction() {
        if (canOpen()) {
            return Action.OPEN;
        } else if (elevator.hasInsideRequests() || elevator.hasOutsideRequests()) {
            return canMove() ? Action.MOVE : Action.REVERSE;
        } else {
            // If the elevator is empty and no one is waiting
            return Action.WAIT;
        }
    }

    private boolean canOpen() {
        // If a person needs to get off
        if (elevator.hasInsideRequestsToFloor(elevator.getFloor())) {
            return true;
        }
        // If the elevator is full
        if (elevator.getPersonCount() == Elevator.MAX_PERSON_COUNT) {
            return false;
        }
        // If a person is to get on
        return elevator.hasOutsideRequestsFromFloor(elevator.getFloor());
    }

    private boolean canMove() {
        // If a person is to get off in the same direction or is waiting in the same direction
        return elevator.hasInsideRequestsInDirection(elevator.getDirection())
                || elevator.hasOutsideRequestsInDirection(elevator.getDirection());
    }
}
