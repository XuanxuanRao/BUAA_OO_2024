package myexceptions;

import com.oocourse.spec2.exceptions.RelationNotFoundException;

import java.util.HashMap;

public class MyRelationNotFoundException extends RelationNotFoundException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id1;
    private final int id2;

    public MyRelationNotFoundException(int id1, int id2) {
        super();
        this.id1 = id1;
        this.id2 = id2;
        record.put(id1, record.getOrDefault(id1, 0) + 1);
        record.put(id2, record.getOrDefault(id2, 0) + 1);
        count++;
    }

    @Override
    public void print() {
        if (id1 < id2) {
            System.out.printf("rnf-%d, %d-%d, %d-%d\n",
                    count, id1, record.get(id1), id2, record.get(id2));
        } else {
            System.out.printf("rnf-%d, %d-%d, %d-%d\n",
                    count, id2, record.get(id2), id1, record.get(id1));
        }
    }
}
