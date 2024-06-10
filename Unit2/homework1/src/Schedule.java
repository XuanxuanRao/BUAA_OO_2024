/*
    * Scheduler.java
    * 每个楼层拥有一个调度器对象Schedule,负责将等待队列waitRequests中的请求分配给电梯的 outsideRequests
 */
import java.util.ArrayList;

public class Schedule implements Runnable {
    private final WaitQueue waitRequests;                   // 等待队列，对 Scheduler 和 Input 可见
    private final ArrayList<Elevator> elevators;

    public Schedule(WaitQueue waitRequests, ArrayList<Elevator> elevators) {
        this.waitRequests = waitRequests;
        this.elevators = elevators;
    }

    @Override
    public void run() {
        while (true) {
            if (waitRequests.isEmpty() && waitRequests.isOver()) {
                for (Elevator elevator : elevators) {
                    elevator.setOver();
                }
                return;
            }
            Request request = waitRequests.popRequest();
            if (request != null) {
                Elevator elevator = elevators.get(request.getElevatorId() - 1);
                elevator.addRequest(request);
            }
        }
    }
}
