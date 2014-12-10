package mods.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mods.utils.Env;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Event.EventType;
import mods.annotation.Token;
import mods.annotation.Trigger;

public class GeneralFilter extends FilterOne {

  public static void main(String[] args)
      throws Exception {

	  GeneralFilter p = new GeneralFilter("target");

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

  public GeneralFilter(String t) {
      if(t.equals("Regulation"))
          type = EventType.Regulation;
      else if(t.equals("MiRNATarget"))
          type = EventType.MiRNATarget;
      else
          type = EventType.General;
    prefix = "";
    readTrigger(0);   
  }

  @Override
  protected Entity getTrigger(Event e3) {

      TriggerMatcher eMatcher = new GeneralTriggerMatcher(triggers);
      if (eMatcher.matches(e3.trigger,getPattern(e3.comment))) {
          return eMatcher.getTrigger();
      }
      return null;
  }

  protected String getPattern(String comment)
  {
	  return comment.substring(comment.indexOf('#')+2,comment.indexOf('['));
  }
  
  @Override
  protected List<Entity> getThemes(Event e3) {
    ThemeMatcher tMatcher = new GeneralThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      return tMatcher.getThemes();
    }
    return Collections.emptyList();
  }
}

class GeneralThemeMatcher extends ThemeMatcher {

  public GeneralThemeMatcher(List<Entity> a1ts, boolean isRafLink) {
    super(a1ts, isRafLink);
    
  }

  @Override
  public boolean matches(Event event, Entity e) {
    this.e = e;
    themes.add(e);
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

class GeneralTriggerMatcher extends TriggerMatcher {

  public GeneralTriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());

    return b;
  }
}
