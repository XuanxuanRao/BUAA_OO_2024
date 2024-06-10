import enums.Action;

public class SingleCarStrategy implements Strategy {
    private final SingleCarElevator elevator;

    public SingleCarStrategy(SingleCarElevator elevator) {
        this.elevator = elevator;
    }

    @Override
    public Action nextAction() {
        if (canOpen()) {
            return Action.OPEN;
        } else if (!elevator.isEmpty() || elevator.getFloor() == elevator.getTransferFloor()) {
            return canMove() ? Action.MOVE : Action.REVERSE;
        } else {
            return elevator.isOver() ? Action.TERMINATE : Action.WAIT;
        }
    }

    private boolean canOpen() {
        // If a person needs to get off or needs to transfer here
        if (elevator.hasInsidePersonsToFloor(elevator.getFloor()) ||
                (elevator.getFloor() == elevator.getTransferFloor()
                        && elevator.hasInsidePersonsToTransfer())) {
            return true;
        }
        // If the elevator is full
        if (elevator.getPersonCount() == elevator.getCapacity()) {
            return false;
        }
        // If a person is to get on
        return elevator.hasOutsidePersonsFromFloor(elevator.getFloor());
    }

    private boolean canMove() {
        // If the elevator is in the transfer floor and no one needs to get off, must leave
        if (elevator.getKind() == 'A'
                && elevator.getFloor() + elevator.getDirection() > elevator.getTransferFloor()) {
            return false;
        } else if (elevator.getKind() == 'B'
                && elevator.getFloor() + elevator.getDirection() < elevator.getTransferFloor()) {
            return false;
        }
        // If a person is to get off in the same direction or is waiting in the same direction
        return elevator.hasInsidePersonsInDirection(elevator.getDirection())
                || elevator.hasOutsidePersonsInDirection(elevator.getDirection());
    }
}
