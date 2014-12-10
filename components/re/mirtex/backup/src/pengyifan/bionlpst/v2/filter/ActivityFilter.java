package pengyifan.bionlpst.v2.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.ArgumentType;
import pengyifan.bionlpst.v2.annotation.Event.EventType;
import pengyifan.bionlpst.v2.annotation.Token;
import pengyifan.bionlpst.v2.annotation.Trigger;

public class ActivityFilter extends FilterOne {

  public static void main(String[] args)
      throws Exception {

    ActivityFilter p = new ActivityFilter();

    if (args.length == 0) {
      args = new String[] { "PMID-1829648" };
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  public ActivityFilter() {
    type = EventType.Activity;
    prefix = "";
    readTrigger(0);
  }

  @Override
  protected Entity getTrigger(Event e3) {

    // if (e3.trigger.from() == 1006) {
    // System.err.println();
    // }

    // if the last word is trigger
    TriggerMatcher eMatcher = new ActivityTriggerMatcher(triggers);
    if (eMatcher.matches(e3.trigger)) {
      Entity trigger = eMatcher.getTrigger();
      return trigger;
    }

    return null;
  }

  @Override
  protected List<Entity> getThemes(Event e3) {
    // if it contains theme
    ThemeMatcher tMatcher = new ActivityThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      List<Entity> theme = tMatcher.getThemes();
      return theme;
    }
    return Collections.emptyList();
  }

  @Override
  protected Event getEvent(Event e3, Entity newTrigger, Entity newTheme) {
    Event e = super.getEvent(e3, newTrigger, newTheme);
    if (e == null) {
      return null;
    }

    boolean isBindingActivity = false;
    if (isBefore(newTrigger.from() - 1, "binding$")) {
      isBindingActivity = true;
    }

    if (isBindingActivity) {
      // trigger
      List<Token> tokens = new ArrayList<Token>();
      // add binding
      Token t = new Token("binding", "JJ", newTrigger.from()
          - "binding ".length(), newTrigger.from() - 1);
      tokens.add(t);
      tokens.addAll(newTrigger.tokens);
      newTrigger = new Entity(newTrigger.id, newTrigger.type, tokens);

      String newTriggerStr = text.substring(newTrigger.from(), newTrigger.to());
      if (newTriggerStr.startsWith("binding complex")) {
        if (!e3.argument.equals(newTheme)) {
          return null;
        }
        if (!e3.comment.startsWith("# Vnorm with Arg")) {
          return null;
        }
      }
      if (newTriggerStr.startsWith("binding complex")) {
        if (!e3.argument.equals(newTheme)) {
          return null;
        }
        if (!e3.comment.startsWith("# Vnorm of Arg")) {
          return null;
        }
      }

      return new Event(e3.filename, EventType.Binding, newTrigger,
          ArgumentType.Theme, newTheme, e3.comment);
    }

    boolean isTranscriptionActivity = false;
    if (isBefore(newTrigger.from() - 1, "transcription$")) {
      isTranscriptionActivity = true;
    }
    if (isTranscriptionActivity) {
      return new Event(e3.filename, EventType.Transcription, newTrigger,
          ArgumentType.Theme, newTheme, e3.comment);
    }

    boolean hasHeterodimer = false;
    if (text.substring(newTrigger.from(), newTrigger.to()).startsWith(
        "heterodimer")) {
      hasHeterodimer = true;
    }
    if (hasHeterodimer) {
      return new Event(e3.filename, EventType.Transcription, newTrigger,
          ArgumentType.Theme, newTheme, e3.comment);
    }

    // if (newTrigger.tokens.getFirst().word.equals("status")
    // && isBefore(newTrigger.from() - 1, "phosphorylation$")) {
    // return new Event(e3.filename, EventType.Phosphorylation, newTrigger,
    // newTheme, e3.comment);
    // }
    //
    // if (newTrigger.tokens.getFirst().word.equals("form")
    // && isBefore(newTrigger.from() - 1, "phosphorylated$")) {
    // return new Event(e3.filename, EventType.Phosphorylation, newTrigger,
    // newTheme, e3.comment);
    // }

    return null;
  }
}

class ActivityThemeMatcher extends ThemeMatcher {

  public ActivityThemeMatcher(List<Entity> a1ts, boolean isRefLink) {
    super(a1ts, isRefLink);
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
        lastWordPattern = "(TRE|SRE|species|TSS|homodimer|mutant|spec|heterodimer|subunit|GAS|coreceptor|pathway|ligand)s?";
        if (Pattern.matches(lastWordPattern, last.word)
            && tokens.size() - 2 >= 0) {
          last = tokens.get(tokens.size() - 2);
        }
      }

      // String lastWordPattern =
      // "(molecule|gene|DNA|homodimer|protein|mutant|spec|product|heterodimer|subunit|GAS|molecule|coreceptor|factor|pathway|ligand)s?";

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
        last = tokens.get(tokens.size() - 3);
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

  @Override
  public boolean lastTwoWord(String word_2, String word_1) {

    if (!isRefLink) {
      return false;
    }

    if (word_2.startsWith("tyrosine") && word_1.startsWith("kinase")) {
      return true;
    }

    if (word_2.startsWith("protein") && word_1.startsWith("complex")) {
      return true;
    }
    if (word_2.startsWith("transcription") && word_1.startsWith("factor")) {
      return true;
    }
    if (word_2.startsWith("NF-kappa") && word_1.startsWith("B")) {
      return true;
    }

    return false;
  }

  public boolean lastThreeWord(String word_3, String word_2, String word_1) {
    if (word_3.startsWith("NF-kappa")
        && word_2.startsWith("B")
        && word_1.startsWith("complex")) {
      return true;
    }
    return false;
  }
}

class ActivityTriggerMatcher extends TriggerMatcher {

  public ActivityTriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {

    String triggers[] = new String[] { //
    "activity", //
        // "subunit",//
        "complex",//
        // "characteristic",//
        // "property",//
        // "site",//
        // "element",//
        // "state",//
        // "sequence",//
        // "protein",//
        // "region",//
        // "domain",//
        // "pattern",//
        // "study",//
        // "genotype",//
        //
        "activities", //
        // "analysis", //
        // "subunits",//
        "complexes",//
        // "characteristic",//
        // "properties",//
        // "sites",//
        // "elements",//
        // "states",//
        // "sequences",//
        // "proteins",//
        // "regions",//
        // "domains",//
        // "patterns",//
        // "studies",//
        // "genotypes",//
        "form", "status", "level",//
        "levels" };

    boolean b = false;
    for (String t : triggers) {
      if (s.equalsIgnoreCase(t)) {
        b = true;
        break;
      }
    }

    // if (!b) {
    // if (s.startsWith("heterodimer")) {
    // b = true;
    // }
    // }
    return b;
  }
}