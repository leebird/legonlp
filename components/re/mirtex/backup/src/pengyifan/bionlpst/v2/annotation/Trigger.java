package pengyifan.bionlpst.v2.annotation;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pengyifan.bionlpst.v2.annotation.Event.EventType;

public class Trigger {

  public EventType   type;
  public String      word;
  public String      lexeme;
  public String      pos;
  public int         freq;
  public String      mark;
  public Set<String> patterns;

  public Trigger() {
    word = "";
    lexeme = "";
    pos = "";
    freq = 0;
    mark = "";
    patterns = new TreeSet<String>();
  }

  @Override
  public String toString() {
    return type + "\t" + word + "\t" + lexeme + "\t" + pos;
  }

  public static boolean isTrigger(List<Trigger> triggers, String word,
      String pos) {
    for (Trigger trigger : triggers) {
      if (word.equals(trigger.word) && pos.equals(trigger.pos)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isTrigger(List<Trigger> triggers, String word) {
    for (Trigger trigger : triggers) {
      if (word.equals(trigger.word)) {
        return true;
      }
    }
    return false;
  }

  public static boolean endsWithTrigger(List<Trigger> triggers, String word) {
    // trigger-xxxx
    // xxx-trigger
    String toks[] = word.split("[-/]");
    if (toks.length == 2) {
      // if (isTrigger(triggers, toks[0])) {
      // return true;
      // }
      if (isTrigger(triggers, toks[1])) {
        return true;
      }
    }
    return false;
  }
}