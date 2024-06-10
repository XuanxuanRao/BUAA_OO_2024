import com.oocourse.spec1.main.Person;

import java.util.HashMap;
import java.util.Objects;

public class MyPerson implements Person {
    private final int id;
    private final String name;
    private final int age;
    private final HashMap<Integer, MyPerson> acquaintance = new HashMap<>();
    private final HashMap<Integer, Integer> value = new HashMap<>();

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
    public boolean isLinked(Person person) {
        return this.equals(person) || acquaintance.containsKey(person.getId());
    }

    @Override
    public int queryValue(Person person) {
        return value.getOrDefault(person.getId(), 0);
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
        acquaintance.put(person.getId(), (MyPerson) person);
        this.value.put(person.getId(), value);
    }

    public void removeAcquaintance(Person person) {
        acquaintance.remove(person.getId());
        value.remove(person.getId());
    }

    public HashMap<Integer, MyPerson> getAcquaintance() {
        return acquaintance;
    }

    public void setValue(Person person, int value) {
        this.value.put(person.getId(), value);
    }

    @Override
    public String toString() {
        return "p" + id;
    }

    public boolean strictEquals(Object obj) {
        if (obj instanceof MyPerson) {
            MyPerson p = (MyPerson) obj;
            return id == p.getId() && name.equals(p.getName()) && age == p.getAge();
        }
        return false;
    }
}
