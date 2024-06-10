package myexceptions;

import com.oocourse.spec3.exceptions.PathNotFoundException;

import java.util.HashMap;

public class MyPathNotFoundException extends PathNotFoundException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id1;
    private final int id2;

    public MyPathNotFoundException(int id1, int id2) {
        super();
        this.id1 = id1;
        this.id2 = id2;
        record.put(id1, record.getOrDefault(id1, 0) + 1);
        if (id1 != id2) {
            record.put(id2, record.getOrDefault(id2, 0) + 1);
        }
        count++;
    }

    @Override
    public void print() {
        if (id1 < id2) {
            System.out.printf("pnf-%d, %d-%d, %d-%d\n",
                    count, id1, record.get(id1), id2, record.get(id2));
        } else {
            System.out.printf("pnf-%d, %d-%d, %d-%d\n",
                    count, id2, record.get(id2), id1, record.get(id1));
        }
    }
}
