package myexceptions;

import com.oocourse.spec3.exceptions.MessageIdNotFoundException;

import java.util.HashMap;

public class MyMessageIdNotFoundException extends MessageIdNotFoundException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id;

    public MyMessageIdNotFoundException(int id) {
        this.id = id;
        record.put(id, record.getOrDefault(id, 0) + 1);
        count++;
    }

    @Override
    public void print() {
        System.out.printf("minf-%d, %d-%d\n", count, id, record.get(id));
    }
}
