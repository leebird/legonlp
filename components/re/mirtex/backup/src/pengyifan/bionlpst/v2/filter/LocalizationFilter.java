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

public class LocalizationFilter extends FilterOne {

  public static void main(String[] args)
      throws Exception {

    LocalizationFilter p = new LocalizationFilter();

    if (args.length == 0) {
      args = new String[] { "PMID-7929104" };
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  public LocalizationFilter() {
    type = EventType.Localization;
    prefix = "";
    readTrigger(0);
  }

  static final Pattern scretTrigger = Pattern
                                        .compile(
                                            "^appearance|secret|transloc|release|local|migrat|import|export|mobil|co[-]local",
                                            Pattern.CASE_INSENSITIVE);

  static final Pattern expTrigger   = Pattern.compile(
                                        "^expre",
                                        Pattern.CASE_INSENSITIVE);

  // static final Pattern preTrigger = Pattern
  // .compile(
  // "^(presen|detect|find|found|direct|appear|detect|accumulation)",
  // Pattern.CASE_INSENSITIVE);
  // static final Pattern nucTrigger = Pattern.compile(
  // "nuclear|nucleus",
  // Pattern.CASE_INSENSITIVE);
  // static final Pattern sufTrigger = Pattern.compile(
  // "surface",
  // Pattern.CASE_INSENSITIVE);

  @Override
  protected Event getEvent(Event e3, Entity newTrigger, Entity newTheme) {
    Event e = super.getEvent(e3, newTrigger, newTheme);
    if (e == null) {
      return null;
    }

    if (scretTrigger.matcher(newTrigger.getLast().word).find()) {
      if (isBefore(newTrigger.from() - 1, "mRNA$")) {
        return null;
      } else {
        return e;
      }
    }

    if (expTrigger.matcher(newTrigger.getLast().word).find()) {
      if (isBefore(newTrigger.from() - 1, "cell[-]surface$")) {
        return e;
      } else {
        return null;
      }
    }

    if (newTrigger.getLast().word.startsWith("detect")) {
      if (isBefore(newTheme.from() - 1, "nuclear$")) {
        return e;
      } else {
        return null;
      }
    }

    if (newTrigger.getLast().word.equals("accumulation")) {
      if (isBefore(newTrigger.from() - 1, "[Nn]uclear$")) {
        return e;
      } else {
        return null;
      }
    }

    if (newTrigger.getLast().word.equals("reservoir")
        || newTrigger.getLast().word.equals("shuttling")) {
      return e;
    }

    if (newTrigger.getLast().word.equals("abundance")) {
      if (isBefore(newTrigger.from() - 1, "[Nn]uclear$")) {
        return e;
      } else {
        return null;
      }
    }

    if (newTrigger.getLast().word.equals("distributed")) {
      if (isBefore(newTheme.from() - 1, "[Nn]uclear$")) {
        return e;
      } else {
        return null;
      }
    }

    return null;

    // boolean hasPresence = false;
    // boolean hasExpress = false;
    //
    // for (Token t : newTrigger.tokens) {
    // if (expTrigger.matcher(t.word).find()) {
    // hasExpress = true;
    // } else if (preTrigger.matcher(t.word).find()) {
    // hasPresence = true;
    // }
    // }
    // if (hasExpress) {
    // int firstDot = text.lastIndexOf('.', newTrigger.from());
    // int lastDot = text.indexOf('.', newTrigger.to());
    // String senText = text.substring(firstDot + 1, lastDot);
    // if (!sufTrigger.matcher(senText).find()) {
    // return null;
    // }
    // } else if (hasPresence) {
    // int firstDot = text.lastIndexOf('.', newTrigger.from());
    // int lastDot = text.indexOf('.', newTrigger.to());
    // String senText = text.substring(firstDot + 1, lastDot);
    // if (!nucTrigger.matcher(senText).find()) {
    // return null;
    // }
    // }
    //
    // return e;
  }

  @Override
  protected Entity getTrigger(Event e3) {

    if (e3.trigger.from() == 524) {
      // System.err.println();
    }

    TriggerMatcher eMatcher = new LocalTriggerMatcher(triggers);
    if (eMatcher.matches(e3.trigger)) {
      return eMatcher.getTrigger();
    }
    return null;
  }

  @Override
  protected List<Entity> getThemes(Event e3) {
    ThemeMatcher tMatcher = new LocalThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      return tMatcher.getThemes();
    }
    return Collections.emptyList();
  }
}

class LocalThemeMatcher extends ThemeMatcher {

  public LocalThemeMatcher(List<Entity> a1ts, boolean isRafLink) {
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

    Entity theme = endsWith(last);
    if (theme != null) {
      themes.add(theme);
    } else {

      String lastWordPattern = "(gene|DNA|protein|product|molecule|factor)s?";
      if (Pattern.matches(lastWordPattern, last.word) && tokens.size() - 2 >= 0) {
        last = tokens.get(tokens.size() - 2);
      }

      if (isRefLink) {

        lastWordPattern = "(complex|complexe|antigen|TRE|SRE|species|TSS|homodimer|mutant|spec|heterodimer|subunit|GAS|coreceptor|pathway|ligand)s?";
        if (Pattern.matches(lastWordPattern, last.word)
            && tokens.size() - 2 >= 0) {
          last = tokens.get(tokens.size() - 2);
        }
      }

      // String lastWordPattern =
      // "(complex|complexe|antigen|gene|DNA|homodimer|protein|spec|product|heterodimer|subunit|GAS|molecule|coreceptor|factor|pathway|ligand)s?";

      if (tokens.size() >= 3
          && lastTwoWord(
              tokens.get(tokens.size() - 2).word,
              tokens.get(tokens.size() - 1).word)) {
        last = tokens.get(tokens.size() - 3);
      }
      if (tokens.size() >= 4
          && lastThreeWord(
              tokens.get(tokens.size() - 3).word,
              tokens.get(tokens.size() - 2).word,
              tokens.get(tokens.size() - 1).word)) {
        last = tokens.get(tokens.size() - 4);
      }
      theme = endsWith(last);

      if (theme != null) {
        themes.add(theme);
      }
    }

    // last word xxx/xxx
    int midIndex = last.word.indexOf('/');
    if (midIndex == -1) {
      midIndex = last.word.indexOf('-');
    }
    if (midIndex != -1) {
      Token firstToken = new Token(last.word.substring(0, midIndex), "NN",
          last.from(), last.from() + midIndex);
      theme = endsWith(firstToken);
      if (theme != null && !themes.isEmpty()) {
        themes.add(theme);
      }
    }
    // last word xxx/xxx/xxx
    if (midIndex != -1) {
      int secondMidIndex = last.word.indexOf('/', midIndex + 1);
      if (secondMidIndex != -1) {
        Token secondToken = new Token(last.word.substring(
            midIndex + 1,
            secondMidIndex), "NN", last.from() + midIndex + 1, last.from()
            + secondMidIndex);
        theme = endsWith(secondToken);
        if (theme != null && !themes.isEmpty()) {
          themes.add(theme);
        }
      }
    }

    return !themes.isEmpty();
  }

  public boolean lastThreeWord(String word_3, String word_2, String word_1) {
    if (!isRefLink) {
      return false;
    }
    if (word_3.startsWith("NF.kappa")
        && word_2.startsWith("B")
        && word_1.startsWith("protein")) {
      return true;
    }

    if (word_3.startsWith("NF-kappa")
        && word_2.startsWith("B")
        && word_1.startsWith("complex")) {
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

    if (word_2.startsWith("protein") && word_1.startsWith("complex")) {
      return true;
    }

    return false;
  }
}

class LocalTriggerMatcher extends TriggerMatcher {

  public LocalTriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());
    return b;
  }
}