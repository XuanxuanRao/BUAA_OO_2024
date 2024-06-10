package myexceptions;

import com.oocourse.spec1.exceptions.PersonIdNotFoundException;

import java.util.HashMap;

public class MyPersonIdNotFoundException extends PersonIdNotFoundException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id;

    public MyPersonIdNotFoundException(int id) {
        super();
        this.id = id;
        record.put(id, record.getOrDefault(id, 0) + 1);
        count++;
    }

    @Override
    public void print() {
        System.out.printf("pinf-%d, %d-%d\n", count, id, record.get(id));
    }
}
