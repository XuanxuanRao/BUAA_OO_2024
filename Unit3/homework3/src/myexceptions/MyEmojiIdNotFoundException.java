package myexceptions;

import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;

import java.util.HashMap;

public class MyEmojiIdNotFoundException extends EmojiIdNotFoundException {
    private static int count = 0;
    private static final HashMap<Integer, Integer> record = new HashMap<>();
    private final int id;

    public MyEmojiIdNotFoundException(int id) {
        this.id = id;
        record.put(id, record.getOrDefault(id, 0) + 1);
        count++;
    }

    @Override
    public void print() {
        System.out.printf("einf-%d, %d-%d\n", count, id, record.get(id));
    }
}
