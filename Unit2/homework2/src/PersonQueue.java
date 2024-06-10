import java.util.ArrayList;
import java.util.Iterator;

public class PersonQueue {
    private final ArrayList<Person> queue;

    public PersonQueue() {
        queue = new ArrayList<>();
    }

    public boolean hasPersonsFromFloor(int floor) {
        for (Person person : queue) {
            if (person.getFromFloor() == floor) {
                return true;
            }
        }
        return false;
    }

    public boolean haspPersonsToFloor(int floor) {
        for (Person person : queue) {
            if (person.getToFloor() == floor) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPersonsInDirection(int direction, int floor, boolean isInside) {
        for (Person person : queue) {
            int compareFloor = isInside ? person.getToFloor() : person.getFromFloor();
            if (direction > 0 ? compareFloor > floor : compareFloor < floor) {
                return true;
            }
        }
        return false;
    }

    public void addPerson(Person person) {
        queue.add(person);
    }

    public Person popPerson() {
        return queue.isEmpty() ? null : queue.remove(0);
    }

    public Person popPersonFromFloor(int fromFloor) {
        Iterator<Person> iterator = queue.iterator();
        Person result = null;
        while (iterator.hasNext()) {
            Person tmp = iterator.next();
            if (tmp.getFromFloor() == fromFloor) {
                result = tmp;
                iterator.remove();
                break;
            }
        }
        return result;
    }

    public Person popPersonToFloor(int toFloor) {
        Iterator<Person> iterator = queue.iterator();
        Person result = null;
        while (iterator.hasNext()) {
            Person tmp = iterator.next();
            if (tmp.getToFloor() == toFloor) {
                result = tmp;
                iterator.remove();
                break;
            }
        }
        return result;
    }
    
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    @Override
    protected Object clone() {
        PersonQueue clone = new PersonQueue();
        for (Person person : queue) {
            clone.addPerson(person.clone());
        }
        return clone;
    }
}
