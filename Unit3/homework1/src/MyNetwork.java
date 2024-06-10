import com.oocourse.spec1.main.Network;
import myexceptions.MyEqualPersonIdException;
import myexceptions.MyEqualRelationException;
import myexceptions.MyPersonIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Person;
import myexceptions.MyRelationNotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class MyNetwork implements Network {
    private int tripleSum;  // number of triple relations, dynamically maintained
    private final HashMap<Integer, MyPerson> persons = new HashMap<>();
    private int lastBlockSum;

    public MyNetwork() {
        tripleSum = 0;
        lastBlockSum = -1;
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return persons.getOrDefault(id, null);
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (containsPerson(person.getId())) {
            throw new MyEqualPersonIdException(person.getId());
        }
        persons.put(person.getId(), (MyPerson) person);
        if (lastBlockSum != -1) {
            lastBlockSum++;
        }
    }

    /**
     * add a relation between two person
     * @param id1   id of person1
     * @param id2   id of person2
     * @param value value of relation
     * @throws PersonIdNotFoundException if id1 or id2 is not found in the network
     * @throws EqualRelationException    if id1 and id2 already have a relation or id1 equals id2
     */
    @Override
    public void addRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualRelationException
    {
        if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (id1 == id2 || getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyEqualRelationException(id1, id2);
        }
        persons.get(id1).addAcquaintance(getPerson(id2), value);
        persons.get(id2).addAcquaintance(getPerson(id1), value);
        tripleSum += (int) persons.values().stream().
                filter(p -> p.getId() != id1 && p.getId() != id2
                        && p.isLinked(getPerson(id1)) && p.isLinked(getPerson(id2))).count();
        lastBlockSum = -1;
    }

    /**
     * modify the value of relation between two person,
     * if oldValue + value > 0, update the value, otherwise, remove the relation
     * @param id1   id of person1
     * @param id2   id of person2
     * @param value new value of relation
     * @throws PersonIdNotFoundException    if id1 or id2 is not found in the network
     * @throws EqualPersonIdException       if id1 and id2 are in the network and id1 == id2
     * @throws RelationNotFoundException    if id != id2 and
     *                                      there is no relationship between id1 and id2
     */
    @Override
    public void modifyRelation(int id1, int id2, int value) throws
            PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException
    {
        if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
        int newValue = persons.get(id1).queryValue(getPerson(id2)) + value;
        if (newValue > 0) {
            persons.get(id1).setValue(getPerson(id2), newValue);
            persons.get(id2).setValue(getPerson(id1), newValue);
        } else {
            persons.get(id1).removeAcquaintance(getPerson(id2));
            persons.get(id2).removeAcquaintance(getPerson(id1));
            tripleSum -= (int) persons.values().stream().
                    filter(p -> p.getId() != id1 && p.getId() != id2
                            && p.isLinked(getPerson(id1)) && p.isLinked(getPerson(id2))).count();
            lastBlockSum = -1;
        }
    }

    /**
     * query the value of relation between two person
     * @param id1 id of person1
     * @param id2 id of person2
     * @return the value of relation between id1 and id2
     * @throws PersonIdNotFoundException if id1 or id2 is not found in the network
     * @throws RelationNotFoundException if there is no relationship between id1 and id2
     */
    @Override
    public int queryValue(int id1, int id2) throws
            PersonIdNotFoundException, RelationNotFoundException
    {
        if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
        return getPerson(id1).queryValue(getPerson(id2));
    }

    /**
     * check whether two person are in the same connected block
     * @param id1 id of person1
     * @param id2 id of person2
     * @exception PersonIdNotFoundException if id1 or id2 is not in the network
     * @return true if they are in the same block
     */
    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        }
        return bfs(id1, id2, new HashSet<>(), true);
    }

    /**
     * @return the number of connected blocks in the network
     */
    @Override
    public int queryBlockSum() {
        if (lastBlockSum == -1) {
            lastBlockSum = 0;
            HashSet<Integer> visited = new HashSet<>();
            for (int id : persons.keySet()) {
                if (!visited.contains(id)) {
                    bfs(id, -1, visited, false);
                    lastBlockSum++;
                }
            }
        }
        return lastBlockSum;
    }

    /**
     * @return the number of triple relations in the network
     */
    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    /**
     * for the UnitTest, it shouldn't be called else
     * @return all persons in the network
     */
    public Person[] getPersons() {
        return persons.values().toArray(new Person[0]);
    }

    /**
     * @param root    the start of the search
     * @param target  the target of the search
     * @param visited the set of visited nodes
     * @param isSearch if true, return when target is found
     * @return return true if target is found(make sense only when isSearch is true)
     */
    private boolean bfs(int root, int target, HashSet<Integer> visited, boolean isSearch) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(root);
        visited.add(root);
        if (isSearch && root == target) {
            return true;
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : persons.get(u).getAcquaintance().keySet()) {
                if (!visited.contains(v)) {
                    if (isSearch && v == target) {
                        return true;
                    }
                    queue.add(v);
                    visited.add(v);
                }
            }
        }
        return false;
    }
}
