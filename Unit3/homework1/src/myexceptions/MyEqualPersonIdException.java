package myexceptions;

import com.oocourse.spec1.exceptions.EqualPersonIdException;

import java.util.HashMap;

public class MyEqualPersonIdException extends EqualPersonIdException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id;

    public MyEqualPersonIdException(int id) {
        super();
        this.id = id;
        record.put(id, record.getOrDefault(id, 0) + 1);
        count++;
    }

    @Override
    public void print() {
        System.out.printf("epi-%d, %d-%d\n", count, id, record.get(id));
    }
}
