import enums.Action;

public class NormalStrategy implements Strategy {
    private final NormalElevator elevator;

    public NormalStrategy(NormalElevator elevator) {
        this.elevator = elevator;
    }

    @Override
    public Action nextAction() {
        if (elevator.isReset()) {
            return Action.RESET;
        } else if (canOpen()) {
            return Action.OPEN;
        } else if (!elevator.isEmpty()) {
            return canMove() ? Action.MOVE : Action.REVERSE;
        } else {
            // If the elevator is empty and no one is waiting for this elevator
            return elevator.isOver() ? Action.TERMINATE : Action.WAIT;
        }
    }

    private boolean canOpen() {
        // If a person needs to get off
        if (elevator.hasInsideRequestsToFloor(elevator.getFloor())) {
            return true;
        }
        // If the elevator is full
        if (elevator.getPersonCount() == elevator.getCapacity()) {
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
