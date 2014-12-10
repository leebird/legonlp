package mods.annotation;

import org.apache.commons.lang3.Range;

public class Token implements Comparable<Token> {

  // [from, to)
  public final String         pos;
  public final String         word;
  public final Range<Integer> range;

  public Token(String word, String pos, int from, int to) {
    this.word = word;
    this.pos = pos;
    range = Range.between(from, to);
  }

  public final int from() {
    return range.getMinimum();
  }

  public final int to() {
    return range.getMaximum();
  }

  public final int offset() {
    return to() - from();
  }

  @Override
  public String toString() {
    return pos + "_" + word + "_" + from() + "_" + to();
  }

  @Override
  public boolean equals(Object obj) {
    Token t = (Token) obj;
    return range.equals(t.range);
  }

  public boolean equals(int from, int to) {
    return range.equals(Range.between(from, to));
  }

  @Override
  public int compareTo(Token t) {
    int c = new Integer(from()).compareTo(t.from());
    if (c == 0) {
      c = new Integer(to()).compareTo(t.to());
    }
    return c;
  }
}
