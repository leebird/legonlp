package mods.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mods.utils.Env;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Event.EventType;
import mods.annotation.Token;
import mods.annotation.Trigger;

public class TranscriptionFilter extends FilterOne {

  public static void main(String[] args)
      throws Exception {

    TranscriptionFilter p = new TranscriptionFilter();

    if (args.length == 0) {
      args = new String[] { "PMID-9799798" };
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  static final Pattern transTrigger = Pattern.compile(
                                        "^trans",
                                        Pattern.CASE_INSENSITIVE);
  static final Pattern transFactor  = Pattern.compile(
                                        "^trans[^ ]+ factor",
                                        Pattern.CASE_INSENSITIVE);
  static final Pattern edTrans      = Pattern.compile("^ed trans");
  static final Pattern mRNA         = Pattern.compile("^(mRNA|RNA|transcript)");
  boolean              hasMRNA;
  boolean              hasTranscript;

  public TranscriptionFilter() {
    type = EventType.Transcription;
    prefix = "";
    // read trigger
    readTrigger(0);
  }

  @Override
  protected Event getEvent(Event e3, Entity newTrigger, Entity newTheme) {

    if (e3.trigger.from() == 351) {
      // System.err.println();
    }

    Event e = super.getEvent(e3, newTrigger, newTheme);
    if (e == null) {
      return null;
    }

    String triggerStr = text.substring(newTrigger.from(), newTrigger.to())
        .toLowerCase();
    if (triggerStr.startsWith("transcri")) {
      return e;
    }
    if (triggerStr.startsWith("mRNA expression")) {
      return e;
    }

    if (hasMRNA) {
      return e;
    }

    // mrna and surface expression
    if ((triggerStr.equals("expression") || triggerStr.equals("production"))
        && isBefore(newTrigger.from() - 1, "mRNA and surface$")) {
      return e;
    }
    if ((triggerStr.equals("expression") || triggerStr.equals("production"))
        && isBefore(newTrigger.from() - 1, "mRNA and surface protein$")) {
      return e;
    }
    // expression xxx transcript
    if ((triggerStr.startsWith("express") || triggerStr.startsWith("produc"))
        && isNext(newTheme.to() + 1, "^transcript")) {
      return e;
    }
    // expression xxx gene transcript
    if ((triggerStr.startsWith("express") || triggerStr.startsWith("produc"))
        && isNext(newTheme.to() + 1, "^gene transcript")) {
      return e;
    }
    // mrna expression
    if ((triggerStr.equals("expression") || triggerStr.equals("production"))
        && isBefore(newTrigger.from() - 1, "mRNA$")) {
      return e;
    }

    // detected
    if ((triggerStr.equals("detected"))
        && (isBefore(newTrigger.from() - 1, "mRNA$") || isNext(
            newTheme.to() + 1,
            "^transcript"))) {
      return e;
    }
    // // synthesis
    // if ((triggerStr.equals("synthesis"))
    // && (isBefore(newTrigger.from() - 1, "mRNA$"))) {
    // return e;
    // }
    // induction
    if ((triggerStr.equals("induction")) && hasMRNA) {
      return e;
    }

    if ((triggerStr.equals("found")) && hasTranscript) {
      return e;
    }
    return null;
  }

  @Override
  protected Entity getTrigger(Event e3) {

    if (e3.trigger.from() == 665) {
      // System.err.println();
    }

    // if the last word is trigger
    TriggerMatcher eMatcher = new TranTriggerMatcher(triggers);
    if (eMatcher.matches(e3.trigger)) {
      Entity trigger = eMatcher.getTrigger();
      return trigger;
    }

    return null;
  }

  @Override
  protected List<Entity> getThemes(Event e3) {
    // if it contains theme
    TranThemeMatcher tMatcher = new TranThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      List<Entity> theme = tMatcher.getThemes();
      hasMRNA = tMatcher.hasMRNA;
      hasTranscript = tMatcher.hasTranscript;
      return theme;
    }
    return Collections.emptyList();
  }
}

class TranThemeMatcher extends ThemeMatcher {

  boolean hasMRNA;
  boolean hasTranscript;

  public TranThemeMatcher(List<Entity> a1ts, boolean isRafLink) {
    super(a1ts, isRafLink);
    hasMRNA = false;
    hasTranscript = false;
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

      // String lastWordPattern =
      // "(PCR|trans[-]activator|construct|gene|DNA|homodimer|protein|mutant|spec|product|heterodimer|subunit|GAS|molecule|coreceptor|mRNA|RNA|MRNA|transcript)s?$";

      if (Pattern.matches("(mRNA|RNA|MRNA)s?", last.word)) {
        hasMRNA = true;
      }
      if (Pattern.matches("(transcript)s?", last.word)) {
        hasTranscript = true;
      }

      String lastWordPattern = "(PCR|trans[-]activator|construct|gene|DNA|protein|product|molecule|factor|mRNA|RNA|MRNA|transcript)s?";
      Matcher m = Pattern.compile(lastWordPattern).matcher(last.word);
      if ((m.find() || (last.pos.equals("RB") && last.word.endsWith("ly")))
          && tokens.size() - 2 >= 0) {
        last = tokens.get(tokens.size() - 2);
      }

      if (isRefLink) {
        lastWordPattern = "(TRE|SRE|species|TSS|homodimer|mutant|spec|heterodimer|subunit|GAS|coreceptor|pathway|ligand)s?";
        m = Pattern.compile(lastWordPattern).matcher(last.word);
        if ((m.find() || (last.pos.equals("RB") && last.word.endsWith("ly")))
            && tokens.size() - 2 >= 0) {
          last = tokens.get(tokens.size() - 2);
        }
      }

      if (tokens.size() >= 3
          && lastTwoWord(
              tokens.get(tokens.size() - 2).word,
              tokens.get(tokens.size() - 1).word)) {
        last = tokens.get(tokens.size() - 3);
      }
      theme = endsWith(last);
      if (theme != null) {
        themes.add(theme);
      }
    }

    // last word (xxx)/...
    int midIndex = last.word.indexOf('/');
    if (midIndex != -1) {
      Token firstToken = new Token(last.word.substring(0, midIndex), "NN",
          last.from(), last.from() + midIndex);
      theme = endsWith(firstToken);
      if (theme != null) {
        themes.add(theme);
      }
    }
    // last word xxx/(xxx)/...
    if (midIndex != -1) {
      int secondMidIndex = last.word.indexOf('/', midIndex + 1);
      if (secondMidIndex != -1) {
        Token secondToken = new Token(last.word.substring(
            midIndex + 1,
            secondMidIndex), "NN", last.from() + midIndex + 1, last.from()
            + secondMidIndex);
        theme = endsWith(secondToken);
        if (theme != null) {
          themes.add(theme);
        }
      }
    }

    // last word (xxx)-...
    midIndex = last.word.lastIndexOf('-');
    if (midIndex != -1) {
      Token firstToken = new Token(last.word.substring(0, midIndex), "NN",
          last.from(), last.from() + midIndex);
      theme = endsWith(firstToken);
      if (theme != null
          && (last.word.substring(midIndex + 1).equals("encoding") || last.word
              .substring(midIndex + 1).equals("mRNA"))) {
        if (last.word.substring(midIndex + 1).equals("mRNA")) {
          hasMRNA = true;
        }
        themes.add(theme);
      }
    }

    return !themes.isEmpty();
  }

  @Override
  public boolean lastTwoWord(String word_2, String word_1) {

    if (!isRefLink) {
      return false;
    }

    if (word_2.startsWith("gene") && word_1.startsWith("transcript")) {
      return true;
    }
    if (word_2.startsWith("sense") && word_1.startsWith("mRNA")) {
      return true;
    }
    if (word_2.startsWith("messenger") && word_1.startsWith("RNA")) {
      return true;
    }
    if (word_2.startsWith("reporter") && word_1.startsWith("gene")) {
      return true;
    }
    if (word_2.startsWith("transcription") && word_1.startsWith("factor")) {
      return true;
    }
    if (word_2.startsWith("fusion") && word_1.startsWith("protein")) {
      return true;
    }

    if (word_2.startsWith("protein") && word_1.startsWith("complex")) {
      return true;
    }

    if (word_2.startsWith("gene") && word_1.startsWith("cluster")) {
      return true;
    }

    return false;
  }
}

class TranTriggerMatcher extends TriggerMatcher {

  public TranTriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());
    if (!b) {
      b = s.equalsIgnoreCase("mRNA expression");
    }
    return b;
  }
}
