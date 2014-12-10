package pengyifan.bionlpst.v2.annotation;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang3.Range;

public class Entity implements Comparable<Entity> {

  final public String             id;
  final public String             type;
  final public LinkedList<Token> tokens;
  final private Range<Integer>    range;

  public Entity(String id, String type, List<Token> tokens) {
    this.id = id;
    this.type = type;
    this.tokens = new LinkedList<Token>(tokens);
    range = Range.between(from(), to());
  }

  public final int from() {
    return getFirst().from();
  }

  public final int to() {
    return getLast().to();
  }

  public final int offset() {
    return to() - from();
  }

  public final Range<Integer> range() {
    return range;
  }

  public final Token getFirst() {
    return tokens.getFirst();
  }

  public final Token getLast() {
    return tokens.getLast();
  }

  @Override
  public String toString() {
    return String.format("T\t%s\t%s", type, tokens);
  }

  @Override
  public boolean equals(Object obj) {
    Entity e = (Entity) obj;
    return tokens.equals(e.tokens);
  }

  public static Entity find(List<Entity> list, Entity e) {
    return Collectionmods.Utils.find(list, Predicatemods.Utils.equalPredicate(e));
  }

  @Override
  public int compareTo(Entity e) {
    int c = new Integer(from()).compareTo(e.from());
    if (c == 0) {
      c = new Integer(to()).compareTo(e.to());
    }
    return c;
  }
}
