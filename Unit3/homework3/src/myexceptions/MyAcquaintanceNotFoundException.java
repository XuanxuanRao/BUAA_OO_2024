package myexceptions;

import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;

import java.util.HashMap;

public class MyAcquaintanceNotFoundException extends AcquaintanceNotFoundException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id;

    public MyAcquaintanceNotFoundException(int id) {
        super();
        this.id = id;
        record.put(id, record.getOrDefault(id, 0) + 1);
        count++;
    }

    @Override
    public void print() {
        System.out.printf("anf-%d, %d-%d\n", count, id, record.get(id));
    }
}
