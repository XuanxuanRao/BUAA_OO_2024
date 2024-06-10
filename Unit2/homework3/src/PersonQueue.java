import java.util.ArrayList;

public class PersonQueue {
    private final ArrayList<Person> queue;

    public PersonQueue() {
        queue = new ArrayList<>();
    }

    public boolean hasPersonsFromFloor(int floor) {
        return queue.stream().anyMatch(person -> person.getFromFloor() == floor);
    }

    public boolean hasPersonsToFloor(int floor) {
        return queue.stream().anyMatch(person -> person.getToFloor() == floor);
    }

    public boolean hasPersonsInDirection(int direction, int floor, boolean isInside) {
        return queue.stream().anyMatch(person -> {
            int compare = isInside ? person.getToFloor() : person.getFromFloor();
            return (direction > 0 && compare > floor) || (direction < 0 && compare < floor);
        });
    }

    public void addPerson(Person person) {
        queue.add(person);
    }

    public Person popPerson() {
        return queue.isEmpty() ? null : queue.remove(0);
    }

    public Person popPersonFromFloor(int fromFloor) {
        return queue.stream()
                .filter(person -> person.getFromFloor() == fromFloor)
                .findFirst()
                .map(person -> {
                    queue.remove(person);
                    return person;
                })
                .orElse(null);
    }

    public Person popPersonToFloor(int toFloor) {
        return queue.stream()
                .filter(person -> person.getToFloor() == toFloor)
                .findFirst()
                .map(person -> {
                    queue.remove(person);
                    return person;
                })
                .orElse(null);
    }

    public Person popPersonToTransfer(int lowerBound, int upperBound) {
        return queue.stream()
                .filter(person -> person.needTransfer(lowerBound, upperBound))
                .findFirst()
                .map(person -> {
                    queue.remove(person);
                    return person;
                })
                .orElse(null);
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

    public boolean hasPersonsToTransfer(int lowerBound, int upperBound) {
        return queue.stream().anyMatch(person -> person.needTransfer(lowerBound, upperBound));
    }

    public void clear() {
        queue.clear();
    }
}
