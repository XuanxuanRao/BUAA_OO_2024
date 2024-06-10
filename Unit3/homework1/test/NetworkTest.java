import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.main.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NetworkTest {
    private static final int testNum = 100;
    private static final Random random = new Random();
    private final MyNetwork network;

    public NetworkTest(MyNetwork network) {
        this.network = network;
    }

    @Parameters
    public static Collection<Object[]> prepareData() {
        Object[][] object = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            object[i] = new Object[]{createNetwork()};
        }
        return Arrays.asList(object);
    }

    private static MyNetwork createNetwork() {
        int n = random.nextInt(100);
        MyNetwork network = new MyNetwork();
        HashSet<Integer> ids = new HashSet<>();
        for (int i = 0; i < n; i++) {
            try {
                network.addPerson(new MyPerson(createId(ids), "oo", random.nextInt(10) + 1));
            } catch (EqualPersonIdException ignored) {
            }
        }
        Person[] persons = network.getPersons();
        int m = n <= 1 ? 0 : random.nextInt(n * (n - 1) / 2);
        for (int i = 0; i < m; i++) {
            int id1 = persons[random.nextInt(n)].getId();
            int id2 = persons[random.nextInt(n)].getId();
            while (id1 == id2 || network.getPerson(id1).isLinked(network.getPerson(id2))) {
                id1 = persons[random.nextInt(n)].getId();
                id2 = persons[random.nextInt(n)].getId();
            }
            try {
                network.addRelation(id1, id2, random.nextInt(10) + 1);
            } catch (PersonIdNotFoundException | EqualRelationException ignored) {
            }
        }
        return network;
    }

    private static int createId(HashSet<Integer> ids) {
        int id = random.nextInt(100) + 1;
        while (ids.contains(id)) {
            id = random.nextInt(100) + 1;
        }
        ids.add(id);
        return id;
    }

    @Test
    public void queryTripleNumTest() {
        /*@ ensures \result ==
          @         (\sum int i; 0 <= i && i < persons.length;
          @             (\sum int j; i < j && j < persons.length;
          @                 (\sum int k; j < k && k < persons.length
          @                     && getPerson(persons[i].getId()).isLinked(getPerson(persons[j].getId()))
          @                     && getPerson(persons[j].getId()).isLinked(getPerson(persons[k].getId()))
          @                     && getPerson(persons[k].getId()).isLinked(getPerson(persons[i].getId()));
          @                     1)));
          @*/
        int expected = 0;
        Person[] old = network.getPersons();
        int n = old.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                for (int k = j + 1; k < n; k++) {
                    if (network.getPerson(old[i].getId()).isLinked(network.getPerson(old[j].getId()))
                            && network.getPerson(old[j].getId()).isLinked(network.getPerson(old[k].getId()))
                            && network.getPerson(old[k].getId()).isLinked(network.getPerson(old[i].getId()))) {
                        expected++;
                    }
                }
            }
        }
        assertEquals(expected, network.queryTripleSum());
        assertEquals(old.length, network.getPersons().length);
        for (int i = 0; i < old.length; i++) {
            assertTrue(((MyPerson)network.getPersons()[i]).strictEquals(old[i]));
        }
    }

}