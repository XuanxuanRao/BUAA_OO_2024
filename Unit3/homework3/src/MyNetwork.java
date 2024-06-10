import com.oocourse.spec3.main.Network;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Tag;
import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import myexceptions.MyEqualPersonIdException;
import myexceptions.MyPersonIdNotFoundException;
import myexceptions.MyEqualRelationException;
import myexceptions.MyRelationNotFoundException;
import myexceptions.MyEqualTagIdException;
import myexceptions.MyTagIdNotFoundException;
import myexceptions.MyAcquaintanceNotFoundException;
import myexceptions.MyPathNotFoundException;
import myexceptions.MyEqualMessageIdException;
import myexceptions.MyEmojiIdNotFoundException;
import myexceptions.MyMessageIdNotFoundException;
import myexceptions.MyEqualEmojiIdException;

import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Objects;
import java.util.List;

public class MyNetwork implements Network {
    private int tripleSum = 0;      // number of triple relations, dynamically maintained
    private int coupleSum = 0;      // number of couple relations, dynamically maintained
    private final HashMap<Integer, MyPerson> persons = new HashMap<>();
    private final HashMap<Integer, MyMessage> messages = new HashMap<>();
    private final HashMap<Integer, Integer> emojiHeats = new HashMap<>();
    private int lastBlockSum = -1;  // number of connected blocks, if -1, need to recalculate
    private static final int ADD = 0;
    private static final int DELETE = 1;
    private static final int MOD = 2;

    @Override
    public boolean containsPerson(int id) { return persons.containsKey(id); }

    @Override
    public Person getPerson(int id) { return persons.getOrDefault(id, null); }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (containsPerson(person.getId())) { throw new MyEqualPersonIdException(person.getId()); }
        persons.put(person.getId(), (MyPerson) person);
        if (lastBlockSum != -1) { lastBlockSum++; }
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
            PersonIdNotFoundException, EqualRelationException {
        if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        else if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyEqualRelationException(id1, id2);
        }
        final MyPerson oldCp1 = persons.get(id1).getBestAcquaintance();
        final MyPerson oldCp2 = persons.get(id2).getBestAcquaintance();
        persons.get(id1).addAcquaintance(getPerson(id2), value);
        persons.get(id2).addAcquaintance(getPerson(id1), value);
        persons.values().forEach(p -> p.updateTagValueSum(id1, id2, value));
        update(persons.get(id1), persons.get(id2), oldCp1, oldCp2, ADD);
    }

    /**
     * modify the value of relation between two person,
     * if oldValue + value > 0, update the value, otherwise, remove the relation.
     * When this called, use 'update' and 'Tag::updateTagValueSum'
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
        if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        else if (id1 == id2) { throw new MyEqualPersonIdException(id1); }
        else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
        }
        int oldValue = persons.get(id1).queryValue(getPerson(id2));
        int newValue = oldValue + value;
        MyPerson oldCp1 = persons.get(id1).getBestAcquaintance();
        MyPerson oldCp2 = persons.get(id2).getBestAcquaintance();
        if (newValue > 0) {
            persons.get(id1).modifyAcquaintance(getPerson(id2), newValue);
            persons.get(id2).modifyAcquaintance(getPerson(id1), newValue);
            persons.values().forEach(p -> p.updateTagValueSum(id1, id2, value));
        } else {
            persons.get(id1).deleteAcquaintance(getPerson(id2));
            persons.get(id2).deleteAcquaintance(getPerson(id1));
            persons.get(id1).delPersonFromAllTags(getPerson(id2));
            persons.get(id2).delPersonFromAllTags(getPerson(id1));
            persons.values().forEach(p -> p.updateTagValueSum(id1, id2, -oldValue));
        }
        update(persons.get(id1), persons.get(id2), oldCp1, oldCp2, newValue > 0 ? MOD : DELETE);
    }

    @Override
    public int queryValue(int id1, int id2) throws
            PersonIdNotFoundException, RelationNotFoundException {
        if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        else if (!getPerson(id1).isLinked(getPerson(id2))) {
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
        if (!containsPerson(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new MyPersonIdNotFoundException(id2); }
        return bfs(id1, id2, new HashMap<>(), true) != -1;
    }

    @Override
    public int queryBlockSum() {
        if (lastBlockSum == -1) {
            lastBlockSum = 0;
            HashMap<Integer, Integer> visited = new HashMap<>();
            for (int id : persons.keySet()) {
                if (!visited.containsKey(id)) {
                    bfs(id, -1, visited, false);
                    lastBlockSum++;
                }
            }
        }
        return lastBlockSum;
    }

    @Override
    public int queryTripleSum() { return tripleSum; }

    /**
     * add a tag for a person
     * @param personId id of the person
     * @param tag tag to be added
     * @throws PersonIdNotFoundException if personId is not in the network
     * @throws EqualTagIdException if person already has this tag
     */
    @Override
    public void addTag(int personId, Tag tag) throws PersonIdNotFoundException, EqualTagIdException
    {
        if (!persons.containsKey(personId)) { throw new MyPersonIdNotFoundException(personId); }
        else if (persons.get(personId).containsTag(tag.getId())) {
            throw new MyEqualTagIdException(tag.getId());
        } else { persons.get(personId).addTag(tag); }
    }

    /**
     * add person1 to the tag of person2
     * @param personId1 id of person1
     * @param personId2 id of person2
     * @param tagId     id of tag
     * @throws PersonIdNotFoundException  if personId1 or personId2 is not in the network
     * @throws RelationNotFoundException  if personId1 and personId2 have no relation
     * @throws TagIdNotFoundException     if person2 doesn't have the tag with tagId
     * @throws EqualPersonIdException     if personId1 equals personId2
     */
    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId) throws
            PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (!persons.containsKey(personId1)) {
            throw new MyPersonIdNotFoundException(personId1);
        } else if (!persons.containsKey(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new MyEqualPersonIdException(personId1);
        } else if (!getPerson(personId1).isLinked(getPerson(personId2))) {
            throw new MyRelationNotFoundException(personId1, personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else if (getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new MyEqualPersonIdException(personId1);
        } else if (getPerson(personId2).getTag(tagId).getSize() <= 1111) {
            getPerson(personId2).getTag(tagId).addPerson(getPerson(personId1));
        }
    }

    /**
     * query the value sum of a person's tag
     * @param personId id of the person
     * @param tagId id of the tag
     * @return the value sum of the tag
     * @throws PersonIdNotFoundException if personId is not in the network
     * @throws TagIdNotFoundException if person doesn't have the tag with tagId
     */
    @Override
    public int queryTagValueSum(int personId, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new MyPersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else { return persons.get(personId).getTag(tagId).getValueSum(); }
    }

    /**
     * query the age var of a person's tag
     * @param personId id of the person
     * @param tagId id of the tag
     * @return the age var of the tag
     * @throws PersonIdNotFoundException if personId is not in the network
     * @throws TagIdNotFoundException if person doesn't have the tag with tagId
     */
    @Override
    public int queryTagAgeVar(int personId, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) { throw new MyPersonIdNotFoundException(personId); }
        else if (!persons.get(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else { return persons.get(personId).getTag(tagId).getAgeVar(); }
    }

    /**
     * remove person1 from person2's tag
     * @param personId1 id of person1
     * @param personId2 id of person2
     * @param tagId     id of tag
     * @throws PersonIdNotFoundException if person1 or person2 is not in the network, or
     *                                   person1 is not in the tag of person2
     * @throws TagIdNotFoundException    if person2 doesn't have the tag with tagId
     */
    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId1)) { throw new MyPersonIdNotFoundException(personId1); }
        else if (!persons.containsKey(personId2)) {
            throw new MyPersonIdNotFoundException(personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else if (!getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new MyPersonIdNotFoundException(personId1);
        } else { getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1)); }
    }

    /**
     * delete a person's tag
     * @param personId  id of the person
     * @param tagId     id of the tag
     * @throws PersonIdNotFoundException if personId is not in the network
     * @throws TagIdNotFoundException    if person doesn't have the tag with tagId
     */
    @Override
    public void delTag(int personId, int tagId) throws
            PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) { throw new MyPersonIdNotFoundException(personId); }
        else if (!persons.get(personId).containsTag(tagId)) {
            throw new MyTagIdNotFoundException(tagId);
        } else { persons.get(personId).delTag(tagId); }
    }

    @Override
    public boolean containsMessage(int id) { return messages.containsKey(id); }

    @Override
    public void addMessage(Message message) throws
            EqualMessageIdException, EmojiIdNotFoundException, EqualPersonIdException {
        int messageId = message.getId();
        if (containsMessage(messageId)) { throw new MyEqualMessageIdException(messageId); }
        else if (message instanceof MyEmojiMessage &&
                !containsEmojiId(((MyEmojiMessage) message).getEmojiId())) {
            throw new MyEmojiIdNotFoundException(((MyEmojiMessage) message).getEmojiId());
        } else if (message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new MyEqualPersonIdException(message.getPerson1().getId());
        } else { messages.put(message.getId(), (MyMessage) message); }
    }

    @Override
    public Message getMessage(int id) { return messages.getOrDefault(id, null); }

    /**
     * send a message(id). If type=0, send to person2; if type=1, send to all persons in tag
     * @param id id of the message
     * @throws RelationNotFoundException  if the two person in the message(type=0) have no relation
     * @throws MessageIdNotFoundException if message with id is not in the network
     * @throws TagIdNotFoundException     if person1 doesn't have the tag in the message(type=1)
     */
    @Override
    public void sendMessage(int id) throws
            RelationNotFoundException, MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) { throw new MyMessageIdNotFoundException(id); }
        MyMessage message = messages.get(id);
        MyPerson p1 = (MyPerson) message.getPerson1();
        MyPerson p2 = (MyPerson) message.getPerson2();
        if (message.getType() == 0 && !p1.isLinked(p2)) {
            throw new MyRelationNotFoundException(p1.getId(), p2.getId());
        } else if (message.getType() == 1 &&
                !message.getPerson1().containsTag(message.getTag().getId())) {
            throw new MyTagIdNotFoundException(message.getTag().getId());
        }
        messages.remove(id);
        if (message.getType() == 0 && !p1.equals(p2)) {
            p1.sendMessage(message);
            p2.receiveMessage(message);
        } else { ((MyTag) message.getTag()).sendMessage(message); }
        if (message instanceof MyEmojiMessage) {
            emojiHeats.put(((MyEmojiMessage) message).getEmojiId(),
                    emojiHeats.get(((MyEmojiMessage) message).getEmojiId()) + 1);
        }
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!persons.containsKey(id)) { throw new MyPersonIdNotFoundException(id); }
        return persons.get(id).getSocialValue();
    }

    @Override
    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!persons.containsKey(id)) { throw new MyPersonIdNotFoundException(id); }
        return persons.get(id).getReceivedMessages();
    }

    @Override
    public boolean containsEmojiId(int id) { return emojiHeats.containsKey(id); }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) { throw new MyEqualEmojiIdException(id); }
        emojiHeats.put(id, 0);
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!persons.containsKey(id)) { throw new MyPersonIdNotFoundException(id); }
        return persons.get(id).getMoney();
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) { throw new MyEmojiIdNotFoundException(id); }
        return emojiHeats.get(id);
    }

    @Override
    public int deleteColdEmoji(int limit) {
        emojiHeats.entrySet().removeIf(entry -> entry.getValue() < limit);
        messages.entrySet().removeIf(entry -> entry.getValue() instanceof EmojiMessage
                && !containsEmojiId(((EmojiMessage) entry.getValue()).getEmojiId()));
        return emojiHeats.size();
    }

    @Override
    public void clearNotices(int personId) throws PersonIdNotFoundException {
        if (!persons.containsKey(personId)) { throw new MyPersonIdNotFoundException(personId); }
        persons.get(personId).clearNotices();
    }

    /**
     * find the acquaintance with the highest value of a person
     * @param id id of the person
     * @return the id of the best acquaintance
     * @throws PersonIdNotFoundException     if person with id is not in the network
     * @throws AcquaintanceNotFoundException if the person has no acquaintance
     */
    @Override
    public int queryBestAcquaintance(int id) throws
            PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!persons.containsKey(id)) { throw new MyPersonIdNotFoundException(id); }
        else if (persons.get(id).getAcquaintances().isEmpty()) {
            throw new MyAcquaintanceNotFoundException(id);
        } else { return persons.get(id).getBestAcquaintance().getId(); }
    }

    @Override
    public int queryCoupleSum() { return coupleSum; }

    /**
     * find the shortest path between two person
     * @param id1 id of person1
     * @param id2 id of person2
     * @return the length of the shortest path between id1 and id2(id1 and id2 are not included)
     * @throws PersonIdNotFoundException if id1 or id2 is not in the network
     * @throws PathNotFoundException if there is no path between id1 and id2
     */
    @Override
    public int queryShortestPath(int id1, int id2) throws
            PersonIdNotFoundException, PathNotFoundException {
        if (!persons.containsKey(id1)) { throw new MyPersonIdNotFoundException(id1); }
        else if (!persons.containsKey(id2)) { throw new MyPersonIdNotFoundException(id2); }
        int distance = bfs(id1, id2, new HashMap<>(), true);
        if (distance == -1) { throw new MyPathNotFoundException(id1, id2); }
        else { return distance - 1; }
    }

    public Person[] getPersons() { return persons.values().toArray(new Person[0]); }

    public Message[] getMessages() { return messages.values().toArray(new Message[0]); }

    public int[] getEmojiIdList() {
        return emojiHeats.keySet().stream().mapToInt(Integer::intValue).toArray();
    }

    public int[] getEmojiHeatList() {
        return emojiHeats.values().stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * @param root    the start of the search
     * @param target  the target of the search
     * @param dist    the distance from root to node
     * @param isSearch if true, return when target is found
     * @return the distance between root and target, if target is not found, return -1
     */
    private int bfs(int root, int target, HashMap<Integer, Integer> dist, boolean isSearch) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(root);
        dist.put(root, 0);
        if (isSearch && root == target) { return 1; }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : persons.get(u).getAcquaintances().keySet()) {
                if (!dist.containsKey(v)) {
                    queue.add(v);
                    dist.put(v, dist.get(u) + 1);
                    if (isSearch && v == target) { return dist.get(v); }
                }
            }
        }
        return -1;
    }

    private void update(MyPerson p1, MyPerson p2, MyPerson oldCp1, MyPerson oldCp2, int op) {
        if (op == ADD || op == DELETE) {
            int tripleSumDelta = (int) persons.values().stream()
                    .filter(p -> !p.equals(p1) && !p.equals(p2) && p.isLinked(p1) && p.isLinked(p2))
                    .count();
            tripleSum += (op == ADD ? tripleSumDelta : -tripleSumDelta);
            lastBlockSum = -1;
        }
        MyPerson newCp1 = p1.getBestAcquaintance();
        MyPerson newCp2 = p2.getBestAcquaintance();
        if (Objects.equals(oldCp1, newCp1) && Objects.equals(oldCp2, newCp2)) { return; }
        boolean flag1 = true;
        boolean flag2 = true;
        if (p1.equals(oldCp2) && p2.equals(oldCp1)) {
            coupleSum--;
            flag1 = false;
        }
        if (p1.equals(newCp2) && p2.equals(newCp1)) {
            coupleSum++;
            flag2 = false;
        }
        help(p1, oldCp1, newCp1, flag1, flag2);
        help(p2, oldCp2, newCp2, flag1, flag2);
    }

    private void help(MyPerson p, MyPerson oldCp, MyPerson newCp, boolean flag1, boolean flag2) {
        if (!Objects.equals(oldCp, newCp)) {
            if (flag1 && oldCp != null && p.equals(oldCp.getBestAcquaintance())) {
                coupleSum--;
            }
            if (flag2 && newCp != null && p.equals(newCp.getBestAcquaintance())) {
                coupleSum++;
            }
        }
    }
}