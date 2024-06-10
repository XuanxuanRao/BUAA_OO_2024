
import java.util.ArrayList;
import java.util.Iterator;

public class WaitQueue {
    private final ArrayList<Request> queue;
    private boolean isOver = false;

    public WaitQueue() {
        queue = new ArrayList<>();
    }

    public synchronized boolean hasRequestsFromFloor(int floor) {
        for (Request request : queue) {
            if (request.getFromFloor() == floor) {
                notifyAll();
                return true;
            }
        }
        notifyAll();
        return false;
    }

    public synchronized boolean hasRequestsToFloor(int floor) {
        for (Request request : queue) {
            if (request.getToFloor() == floor) {
                notifyAll();
                return true;
            }
        }
        notifyAll();
        return false;
    }

    public synchronized boolean hasRequestsInDirection(int direction, int floor, boolean isInside) {
        for (Request request : queue) {
            int compareFloor = isInside ? request.getToFloor() : request.getFromFloor();
            if (direction > 0 ? compareFloor > floor : compareFloor < floor) {
                notifyAll();
                return true;
            }
        }
        notifyAll();
        return false;
    }

    public synchronized void addRequest(Request request) {
        queue.add(request);
        notifyAll();
    }

    public synchronized Request popRequest() {
        while (queue.isEmpty() && !isOver) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (queue.isEmpty()) {
            return null;
        }
        Request request = queue.get(0);
        queue.remove(0);
        notifyAll();
        return request;
    }

    public synchronized Request popRequest(int fromFloor) {
        while (queue.isEmpty() && !isOver) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (queue.isEmpty()) {
            return null;
        }
        Request request = null;
        Iterator<Request> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Request temp = iterator.next();
            if (temp.getFromFloor() == fromFloor) {
                request = temp;
                iterator.remove();
                break;
            }
        }
        notifyAll();
        return request;
    }

    public synchronized void setOver() {
        this.isOver = true;
        notifyAll();
    }

    public synchronized boolean isOver() {
        notifyAll();
        return isOver;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return queue.isEmpty();
    }

}
