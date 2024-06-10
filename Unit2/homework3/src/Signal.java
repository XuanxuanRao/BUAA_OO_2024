/**
 * The Signal class represents a signal whether two singleCarElevator can reach the transferFloor.
 * It provides synchronized methods to set the signal as occupied or free.
 */
public class Signal {
    private boolean isOccupied = false;

    public synchronized void setOccupied() {
        while (isOccupied) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        isOccupied = true;
        notifyAll();
    }

    public synchronized void setFree() {
        isOccupied = false;
        notifyAll();
    }

}
