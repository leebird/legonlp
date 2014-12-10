package pengyifan.bionlpst.v2.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Token;
import pengyifan.bionlpst.v2.annotation.Trigger;

public abstract class TriggerMatcher {

  protected Entity        e;
  protected Entity        trigger;
  protected List<Trigger> triggers;

  public TriggerMatcher(Collection<Trigger> triggers) {
    this.triggers = new LinkedList<Trigger>(triggers);
  }

  public boolean matches(Entity e) {
    this.e = e;
    Token last = e.getLast();
    if (isTrigger(last.word)) {
      trigger = new Entity(e.id, e.type, Collections.singletonList(last));
      return true;
    }
    // xxx-(xxx)
    int midIndex = last.word.lastIndexOf('-');
    if (midIndex != -1) {
      Token lastToken = new Token(last.word.substring(midIndex + 1), "NN",
          last.from() + midIndex + 1, last.to());
      if (isTrigger(lastToken.word)) {
        trigger = new Entity(e.id, e.type, Collections.singletonList(lastToken));
        return true;
      }
    }
    // xxx/xxx
    midIndex = last.word.lastIndexOf('/');
    if (midIndex != -1) {
      Token lastToken = new Token(last.word.substring(midIndex + 1), "NN",
          last.from() + midIndex + 1, last.to());
      if (isTrigger(lastToken.word)) {
        trigger = new Entity(e.id, e.type, Collections.singletonList(lastToken));
        return true;
      }
    }

    return false;
  }

  public Entity getTrigger() {
    return trigger;
  }

  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());
    return b;
  }

}
