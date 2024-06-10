import enums.Action;

public abstract class Elevator implements Runnable {
    private final int id;
    private int floor;                                  // 电梯当前所处楼层
    private int direction;                              // 电梯的运行方向
    private int capacity;                               // 电梯容量，默认为 6，可能被 RESET 指令修改
    private int moveTime;                               // 电梯移动时间，默认为 400，可能被 RESET 指令修改
    private boolean isOver;                             // 电梯是否可以结束运行
    private final PersonQueue outsidePersons;           // 已被分配要上这部电梯的乘客，对 Elevator Scheduler 可见
    public static final int OPEN_TIME = 200;            // 电梯开门时间
    public static final int CLOSE_TIME = 200;           // 电梯关门时间
    public static final int RESET_TIME = 1200;          // 电梯重置时间
    private Strategy strategy = null;                   // 电梯的捎带策略
    private boolean exist = true;

    public Elevator(int id, PersonQueue outsidePersons) {
        this.id = id;
        this.floor = 1;
        this.direction = 1;
        this.capacity = 6;
        this.moveTime = 400;
        this.isOver = false;
        this.outsidePersons = outsidePersons;
    }

    public Elevator(int id, int floor, int direction, int capacity, int moveTime) {
        this.id = id;
        this.floor = floor;
        this.direction = direction;
        this.capacity = capacity;
        this.moveTime = moveTime;
        this.isOver = false;
        this.outsidePersons = new PersonQueue();
    }

    public int getId() {
        return id;
    }

    public int getFloor() {
        return floor;
    }

    protected void setFloor(int floor) {
        this.floor = floor;
    }

    public int getDirection() {
        return direction;
    }

    protected void setDirection(int direction) {
        this.direction = direction;
    }

    public int getCapacity() {
        return capacity;
    }

    protected void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getMoveTime() {
        return moveTime;
    }

    protected void setMoveTime(int moveTime) {
        this.moveTime = moveTime;
    }

    public Action nextAction() {
        return strategy.nextAction();
    }

    protected void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    protected PersonQueue getOutsidePersons() {
        return outsidePersons;
    }

    public boolean isOver() {
        synchronized (outsidePersons) {
            return isOver;
        }
    }

    public void setOver() {
        synchronized (outsidePersons) {
            isOver = true;
            outsidePersons.notifyAll();
        }
    }

    public synchronized void setExist(boolean exist) {
        this.exist = exist;
    }

    public synchronized boolean exist() {
        return this.exist;
    }

    public abstract void run();

    public abstract boolean isEmpty();

    protected abstract void updatePosition();

    protected abstract void open();

    protected abstract void close();

    protected abstract void in();

    protected abstract void out();

}
