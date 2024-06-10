import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.RedEnvelopeMessage;
import com.oocourse.spec3.main.Tag;

import java.util.HashMap;
import java.util.Objects;

public class MyTag implements Tag {
    private final int id;
    private final HashMap<Integer, MyPerson> persons;
    private int valueSum;       // sum of values of all relations, dynamically maintained
    private int ageSum;         // sum of ages of all persons, dynamically maintained
    private long agePowSum;     // sum of ages^2 of all persons, dynamically maintained

    public MyTag(int id) {
        this.id = id;
        persons = new HashMap<>();
        valueSum = 0;
        ageSum = 0;
        agePowSum = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    /**
     * add a person (not in the tag) to tag.
     * @param person person to be added
     */
    @Override
    public void addPerson(Person person) {
        persons.put(person.getId(), (MyPerson) person);
        ageSum += person.getAge();
        agePowSum += (long) person.getAge() * person.getAge();
        for (MyPerson p : persons.values()) {
            valueSum += 2 * p.queryValue(person);
        }
    }

    @Override
    public boolean hasPerson(Person person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        return valueSum;
    }

    @Override
    public int getAgeMean() {
        return persons.isEmpty() ? 0 : ageSum / persons.size();
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        }
        int mean = getAgeMean();
        return (int)(agePowSum - 2 * mean * ageSum + mean * mean * persons.size()) / persons.size();
    }

    /**
     * remove a person (already in the tag) from tag.
     * @param person person to be removed
     */
    @Override
    public void delPerson(Person person) {
        if (!persons.containsKey(person.getId())) {
            return;
        }
        persons.remove(person.getId());
        ageSum -= person.getAge();
        agePowSum -= (long) person.getAge() * person.getAge();
        for (MyPerson p : persons.values()) {
            valueSum -= 2 * p.queryValue(person);
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Tag && ((Tag) obj).getId() == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * maintain the `valueSum` of the tag.
     * When adding, deleting or modifying a relation, this method should be called.
     * @param personId1  person1's id
     * @param personId2  person2's id
     * @param deltaValue the change of value, newValue - oldValue
     */
    public void updateValueSum(int personId1, int personId2, int deltaValue) {
        if (persons.containsKey(personId1) && persons.containsKey(personId2)) {
            valueSum += 2 * deltaValue;
        }
    }

    public void sendMessage(MyMessage message) {
        if (message.getType() != 1) {
            throw new RuntimeException("Invalid Message");
        }
        message.getPerson1().addSocialValue(message.getSocialValue());
        persons.values().forEach(p -> p.addSocialValue(message.getSocialValue()));
        if (message instanceof RedEnvelopeMessage && getSize() > 0) {
            int moneyInEnvelope = ((RedEnvelopeMessage) message).getMoney() / getSize();
            persons.values().forEach(p -> p.addMoney(moneyInEnvelope));
            message.getPerson1().addMoney(-moneyInEnvelope * getSize());
        }
    }
}
