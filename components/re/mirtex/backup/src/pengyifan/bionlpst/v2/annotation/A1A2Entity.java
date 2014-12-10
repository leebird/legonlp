package pengyifan.bionlpst.v2.annotation;

import org.apache.commons.lang3.Range;

public class A1A2Entity {

  private final String         id;
  private final Range<Integer> range;

  A1A2Entity(String id, int from, int to) {
    this.id = id;
    range = Range.between(from, to);
  }

  public String id() {
    return id;
  }

  public Range<Integer> range() {
    return range;
  }
}
