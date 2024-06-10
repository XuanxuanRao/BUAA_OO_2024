import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.NoticeMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MyPerson implements Person {
    private final int id;
    private final String name;
    private final int age;
    private int socialValue = 0;
    private int money = 0;
    private final HashMap<Integer, MyPerson> acquaintances = new HashMap<>();
    private final HashMap<Integer, Integer> values = new HashMap<>();
    private final HashMap<Integer, MyTag> tags = new HashMap<>();
    private final ArrayList<MyMessage> messages = new ArrayList<>();
    private MyPerson bestAcquaintance = null;
    private final Comparator<Person> comparator = Comparator.comparingInt(this::queryValue)
                                        .thenComparing(Person::getId, Comparator.reverseOrder());

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int tagId) {
        return tags.containsKey(tagId);
    }

    @Override
    public Tag getTag(int tagId) {
        return tags.getOrDefault(tagId, null);
    }

    /**
     * add a tag (not belong to the person) to person
     * @param tag tag to be added
     */
    @Override
    public void addTag(Tag tag) {
        tags.put(tag.getId(), (MyTag) tag);
    }

    @Override
    public void delTag(int tagId) {
        tags.remove(tagId);
    }

    @Override
    public boolean isLinked(Person person) {
        return this.equals(person) || acquaintances.containsKey(person.getId());
    }

    @Override
    public int queryValue(Person person) {
        return values.getOrDefault(person.getId(), 0);
    }

    @Override
    public void addSocialValue(int num) {
        socialValue += num;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    @Override
    public List<Message> getReceivedMessages() {
        return new ArrayList<>(messages.subList(0, Math.min(5, messages.size())));
    }

    @Override
    public void addMoney(int num) {
        money += num;
    }

    @Override
    public int getMoney() {
        return money;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Person && ((Person) obj).getId() == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addAcquaintance(Person person, int value) {
        acquaintances.put(person.getId(), (MyPerson) person);
        this.values.put(person.getId(), value);
        if (bestAcquaintance == null || value > queryValue(bestAcquaintance) ||
            value == queryValue(bestAcquaintance) && person.getId() < bestAcquaintance.getId()) {
            bestAcquaintance = (MyPerson) person;
        }
    }

    public void modifyAcquaintance(Person person, int newValue) {
        this.values.put(person.getId(), newValue);
        bestAcquaintance = null;
    }

    public void deleteAcquaintance(Person person) {
        acquaintances.remove(person.getId());
        values.remove(person.getId());
        if (person.equals(bestAcquaintance)) {
            bestAcquaintance = null;
        }
    }

    public HashMap<Integer, MyPerson> getAcquaintances() {
        return acquaintances;
    }

    @Override
    public String toString() {
        return "P" + id;
    }

    public boolean strictEquals(Object obj) {
        if (obj instanceof MyPerson) {
            MyPerson p = (MyPerson) obj;
            return id == p.getId() && name.equals(p.getName()) && age == p.getAge();
        }
        return false;
    }

    /**
     * find the person's acquaintance with the highest value,
     * if the person has no acquaintance, return null
     * @return best acquaintance
     */
    public MyPerson getBestAcquaintance() {
        if (bestAcquaintance == null) {
            bestAcquaintance = acquaintances.values().stream().max(comparator).orElse(null);
        }
        return bestAcquaintance;
    }

    public void updateTagValueSum(int id1, int id2, int deltaValue) {
        this.tags.values().forEach(tag -> tag.updateValueSum(id1, id2, deltaValue));
    }

    public void delPersonFromAllTags(Person person) {
        this.tags.values().forEach(tag -> tag.delPerson(person));
    }

    public void sendMessage(MyMessage message) {
        addSocialValue(message.getSocialValue());
        if (message instanceof MyRedEnvelopeMessage) {
            addMoney(-((MyRedEnvelopeMessage) message).getMoney());
        }
    }

    public void receiveMessage(MyMessage message) {
        messages.add(0, message);
        addSocialValue(message.getSocialValue());
        if (message instanceof MyRedEnvelopeMessage) {
            addMoney(((MyRedEnvelopeMessage) message).getMoney());
        }
    }

    public void clearNotices() {
        messages.removeIf(myMessage -> myMessage instanceof NoticeMessage);
    }
}
