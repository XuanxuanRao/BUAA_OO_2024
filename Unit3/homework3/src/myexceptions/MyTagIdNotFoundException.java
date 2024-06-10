package myexceptions;

import com.oocourse.spec3.exceptions.TagIdNotFoundException;

import java.util.HashMap;

public class MyTagIdNotFoundException extends TagIdNotFoundException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id;

    public MyTagIdNotFoundException(int id) {
        super();
        this.id = id;
        record.put(id, record.getOrDefault(id, 0) + 1);
        count++;
    }

    @Override
    public void print() {
        System.out.printf("tinf-%d, %d-%d\n", count, id, record.get(id));
    }
}
