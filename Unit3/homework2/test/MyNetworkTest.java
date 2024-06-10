import com.oocourse.spec2.exceptions.*;
import com.oocourse.spec2.main.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MyNetworkTest {
    private static final int testNumber = 100;
    private final MyNetwork network;
    private final ArrayList<int[]> instructions;
    private static final Random random = new Random();

    public MyNetworkTest(MyNetwork network, ArrayList<int[]> instructions) {
        this.network = network;
        this.instructions = instructions;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getData() throws EqualPersonIdException {
        Object[][] object = new Object[testNumber][];
        for (int i = 0; i < testNumber; i++) {
            int n = random.nextInt(15) + 1;
            int m = n == 1 ? 0 : random.nextInt(n * (n - 1) / 2) + 1;
            MyNetwork network = createNetwork(n, m);
            object[i] = new Object[]{network, createInstructions(network, n)};
        }
        return Arrays.asList(object);
    }

    private static MyNetwork createNetwork(int n, int m) throws EqualPersonIdException {
        MyNetwork network = new MyNetwork();
        ArrayList<MyPerson> persons = new ArrayList<>();
        for (int i = -1; i <= n - 2; i++) {
            persons.add(new MyPerson(i, "P" + i, i));
            network.addPerson(persons.get(i + 1));
        }
        for (int i = 0; i < m; i++) {
            int id1 = persons.get(random.nextInt(n)).getId();
            int id2 = persons.get(random.nextInt(n)).getId();
            while (network.getPerson(id1).isLinked(network.getPerson(id2))) {
                id1 = persons.get(random.nextInt(n)).getId();
                id2 = persons.get(random.nextInt(n)).getId();
            }
            try {
                network.addRelation(id1, id2, random.nextInt(10) + 1);
            } catch (PersonIdNotFoundException | EqualRelationException ignored) {
            }
        }
        return network;
    }

    private static ArrayList<int[]> createInstructions(MyNetwork network, int n) {
        ArrayList<int[]> instructions = new ArrayList<>();
        if (n == 1) {
            return instructions;
        }
        for (int i = 0; i < 3; i++) {
            int[] instruction = new int[3];
            int id1 = random.nextInt(n) - 1;
            int id2 = random.nextInt(n) - 1;
            while (id1 == id2 || !network.getPerson(id1).isLinked(network.getPerson(id2))) {
                id1 = random.nextInt(n) - 1;
                id2 = random.nextInt(n) - 1;
            }
            instruction[0] = id1;
            instruction[1] = id2;
            instruction[2] = -random.nextInt(10);
            instructions.add(instruction);
        }
        return instructions;
    }

    @Test
    public void queryCoupleSum() throws RelationNotFoundException, PersonIdNotFoundException, EqualPersonIdException, AcquaintanceNotFoundException {
        Person[] old = network.getPersons();
        assertEquals(getExpectedCoupleSum(network.getPersons()), network.queryCoupleSum());
        assertEquals(old.length, network.getPersons().length);
        for (Person person : old) {
            assertTrue(((MyPerson) person).strictEquals(network.getPerson(person.getId())));
        }
        for (int[] instruction : instructions) {
            int id1 = instruction[0];
            int id2 = instruction[1];
            int deltaValue = instruction[2];
            if (!network.getPerson(id1).isLinked(network.getPerson(id2))) {
                continue;
            }
            network.modifyRelation(id1, id2, deltaValue);
            assertEquals(getExpectedCoupleSum(network.getPersons()), network.queryCoupleSum());
            assertEquals(old.length, network.getPersons().length);
            for (Person person : old) {
                assertTrue(((MyPerson) person).strictEquals(network.getPerson(person.getId())));
            }
        }
    }

    private int getExpectedCoupleSum(Person[] persons) throws AcquaintanceNotFoundException, PersonIdNotFoundException {
        int res = 0;
        for (int i = 0; i < persons.length; i++) {
            for (int j = i + 1; j < persons.length; j++) {
                if (hasAcquaintance(persons[i], persons) &&
                    hasAcquaintance(persons[j], persons) &&
                    network.queryBestAcquaintance(persons[i].getId()) == persons[j].getId() &&
                    network.queryBestAcquaintance(persons[j].getId()) == persons[i].getId()) {
                    res++;
                }
            }
        }
        return res;
    }

    private boolean hasAcquaintance(Person person, Person[] persons) {
        for (Person p : persons) {
            if (!p.equals(person) && p.isLinked(person)) {
                return true;
            }
        }
        return false;
    }

}