import com.oocourse.spec3.exceptions.*;
import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MyNetworkTest {
    private static final int TEST_NUMBER = 10;
    private final MyNetwork network;
    static Random random = new Random();
    enum MessageKind {
        NOTICE, RED_ENVELOPE, EMOJI
    }

    public MyNetworkTest(MyNetwork network) {
        this.network = network;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getData() throws RelationNotFoundException, TagIdNotFoundException, PersonIdNotFoundException, EmojiIdNotFoundException, EqualRelationException, EqualTagIdException, EqualPersonIdException, EqualMessageIdException, EqualEmojiIdException, MessageIdNotFoundException {
        Object[][] object = new Object[TEST_NUMBER][];
        for (int i = 0; i < TEST_NUMBER; i++) {
            int n = random.nextInt(50) + 1;
            object[i] = new Object[]{createNetwork(n)};
        }
        return Arrays.asList(object);
    }

    private static MyNetwork createNetwork(int n) throws EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException, RelationNotFoundException, TagIdNotFoundException, PersonIdNotFoundException, EqualRelationException, EqualTagIdException, EqualEmojiIdException, MessageIdNotFoundException {
        MyNetwork network = new MyNetwork();
        int range = n <= 2 ? 1 : n / 3;
        for (int i = 0; i < n; i++) {
            MessageKind kind = MessageKind.values()[random.nextInt(3)];
            if (kind != MessageKind.EMOJI) {
                kind = MessageKind.values()[random.nextInt(3)];
            }
            int type = random.nextInt(2);
            MyMessage message = null;
            MyPerson person1 = new MyPerson(i, "P", random.nextInt(200) + 1);
            network.addPerson(person1);
            MyPerson person2 =  new MyPerson(i + 100, "P", random.nextInt(200) + 1);
            network.addPerson(person2);
            network.addRelation(person1.getId(), person2.getId(), 5);
            MyTag tag = null;
            if (type == 1) {
                tag = new MyTag(i);
                network.addTag(person1.getId(), tag);
                network.addPersonToTag(person2.getId(), person1.getId(), tag.getId());
            }
            switch (kind) {
                case NOTICE:
                    if (type == 0) {
                        message = new MyNoticeMessage(i, "Notice", person1, person2);
                    } else {
                        message = new MyNoticeMessage(i, "Notice", person1, tag);
                    }
                    break;
                case RED_ENVELOPE:
                    if (type == 0) {
                        message = new MyRedEnvelopeMessage(i, 20, person1, person2);
                    } else {
                        message = new MyRedEnvelopeMessage(i, 20, person1, tag);
                    }
                    break;
                case EMOJI:
                    int emojiId = random.nextInt(range);
                    if (!network.containsEmojiId(emojiId)) {
                        network.storeEmojiId(emojiId);
                    }
                    if (type == 0) {
                        message = new MyEmojiMessage(i, emojiId, person1, person2);
                    } else {
                        message = new MyEmojiMessage(i, emojiId, person1, tag);
                    }
            }
            network.addMessage(message);
            if (random.nextInt(2) == 0) {
                network.sendMessage(message.getId());
            }
        }
        return network;
    }

    @Test
    public void deleteColdEmoji() {
        Message[] oldMessages = network.getMessages();
        int[] oldEmojiIds = network.getEmojiIdList();
        int[] oldHeats = network.getEmojiHeatList();
        int res = network.deleteColdEmoji(1);
        int expected = 0;
        for (int i = 0; i < oldEmojiIds.length; i++) {
            if (oldHeats[i] < 1) {
                assertFalse(network.containsEmojiId(oldEmojiIds[i]));
            } else {
                assertTrue(network.containsEmojiId(oldEmojiIds[i]));
                expected++;
            }
        }
        assertEquals(expected, res);
        Message[] newMessages = network.getMessages();
        for (Message m : oldMessages) {
            if (m instanceof MyEmojiMessage) {
                if (network.containsEmojiId(((MyEmojiMessage) m).getEmojiId())) {
                    assertTrue(Arrays.asList(newMessages).contains(m));
                } else {
                    assertFalse(Arrays.asList(newMessages).contains(m));
                }
            } else {
                assertTrue(Arrays.asList(newMessages).contains(m));
            }
        }
    }
}