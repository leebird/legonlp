package pengyifan.bionlpst.v2.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;
import pengyifan.bionlpst.v2.annotation.Token;
import pengyifan.bionlpst.v2.annotation.Trigger;

public class RegulationFilter extends FilterOne {

  public static void main(String[] args)
      throws Exception {

	  RegulationFilter p = new RegulationFilter();

      if (args.length != 1) {
          args = new String[] { "PMID-10037751" };
          p.debug = true;
      }

      if (args.length == 1) {
          System.err.println("one file: " + args[0] + "!");
          p.processFile(Env.DIR, args[0]);
      } else {
          System.err.println("no file!");
      }
  }

  public RegulationFilter() {
    type = EventType.Regulation;
    prefix = "";
    readTrigger(0);
  }

  @Override
  protected Entity getTrigger(Event e3) {

    if (e3.comment.startsWith("# Arg Vact")
        || e3.comment.startsWith("# Arg does something by Vvbg")) {
      return null;
    } else {
      // detect boundary
      TriggerMatcher eMatcher = new RegulateTriggerMatcher(triggers);
      if (eMatcher.matches(e3.trigger)) {
        return eMatcher.getTrigger();
      }
      return null;
    }
  }

  @Override
  protected List<Entity> getThemes(Event e3) {
    ThemeMatcher tMatcher = new RegulateThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      return tMatcher.getThemes();
    }
    return Collections.emptyList();
  }

}

class RegulateThemeMatcher extends ThemeMatcher {

  public RegulateThemeMatcher(List<Entity> a1ts, boolean isRafLink) {
    super(a1ts, isRafLink);
  }

  @Override
  public boolean matches(Event event, Entity e) {
    this.e = e;
    LinkedList<Token> tokens;

    // split in
    int indexIn = getInIndex(e.tokens);
    if (indexIn != -1) {
      tokens = new LinkedList<Token>(e.tokens.subList(0, indexIn));
    } else {
      tokens = new LinkedList<Token>(e.tokens);
    }

    // has WDT
    for (Token t : tokens) {
      if (t.pos.equals("WDT")) {
        return false;
      }
    }

    // get last
    assert !tokens.isEmpty() : e;
    Token last = tokens.getLast();

    //Entity theme = endsWith(last);
    
    Entity theme = null;
    
    // ensure the theme is a ION word
    //if (last.word.endsWith("tion") ||
    //	last.word.endsWith("sion") ||
    //	last.word.endsWith("ment") ||
    //	last.word.endsWith("sis"))
    		theme = e;
    
    if (theme != null) {
      themes.add(theme);
    } 

    return !themes.isEmpty();
  }

  public boolean lastThreeWord(String word_3, String word_2, String word_1) {

    if (!isRefLink) {
      return false;
    }
    if (word_3.startsWith("NF-kappa")
        && word_2.startsWith("B")
          && word_1.startsWith("protein")) {
      return true;
    }

    if (word_3.startsWith("NF-kappa")
        && word_2.startsWith("B")
          && word_1.startsWith("complex")) {
      return true;
    }

    if (word_3.startsWith("transcription")
        && word_2.startsWith("start")
          && word_1.startsWith("site")) {
      return true;
    }
    return false;
  }

  @Override
  public boolean lastTwoWord(String word_2, String word_1) {
    if (!isRefLink) {
      return false;
    }
    if (word_2.startsWith("transcription") && word_1.startsWith("factor")) {
      return true;
    }
    if (word_2.startsWith("NF-kappa") && word_1.startsWith("B")) {
      return true;
    }
    if (word_2.startsWith("MAP") && word_1.startsWith("kinases")) {
      return true;
    }
    if (word_2.startsWith("fusion") && word_1.startsWith("protein")) {
      return true;
    }

    if (word_2.startsWith("protein") && word_1.startsWith("complex")) {
      return true;
    }
    if (word_2.startsWith("promoter") && word_1.startsWith("elements")) {
      return true;
    }

    if (word_2.startsWith("enhancer") && word_1.startsWith("motif")) {
      return true;
    }
    if (word_2.startsWith("NF-kappaB") && word_1.startsWith("heterodimers")) {
      return true;
    }

    return false;
  }
}

class RegulateTriggerMatcher extends TriggerMatcher {

  public RegulateTriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());
    if (!b) {
      if (s.endsWith("regulated") || s.endsWith("regulate") || s.endsWith("regulates")) {
        b = true;
      }
    }
    return b;
  }
}
