package pengyifan.bionlpst.v2.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;
import pengyifan.bionlpst.v2.annotation.Token;
import pengyifan.bionlpst.v2.annotation.Trigger;

public class GeneExpressionFilter extends FilterOne {

  protected List<Event>  otherA3es;
  protected List<Entity> otherA3ts;

  public static void main(String[] args)
      throws Exception {

    GeneExpressionFilter p = new GeneExpressionFilter();

    if (args.length == 0) {
      args = new String[] { "PMID-8645254" };
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  public GeneExpressionFilter() {
    type = EventType.Gene_expression;
    prefix = "";
    readTrigger(0);
  }

  @Override
  protected void readResource(String dir, String filename) {
    super.readResource(dir, filename);
    otherA3es = new ArrayList<Event>();
    otherA3ts = new ArrayList<Entity>();

    for (EventType t : EventType.values()) {
      if (t == EventType.Gene_expression) {
        continue;
      }
      File f = new File(dir + "/" + filename + ".a3." + t);
      if (!f.exists()) {
        continue;
      }
      // List<Entity> ts = A3EntityReader.readEntities(f.getAbsolutePath());
      // List<Event> es = A3EventReader.readEvents(f.getAbsolutePath(), ts);
      // otherA3ts.addAll(ts);
      // otherA3es.addAll(es);
    }
  }

  static final Pattern mRNA = Pattern.compile(
                                "mRNAs?|RNAs?",
                                Pattern.CASE_INSENSITIVE);

  @Override
  protected Event getEvent(Event e3, Entity newTrigger, Entity newTheme) {
    Event e = super.getEvent(e3, newTrigger, newTheme);
    if (e == null) {
      return null;
    }
    if (belongsToOthers(e)) {
      return null;
    }

    boolean hasMrna = false;
    for (Token t : e3.trigger.tokens) {
      if (mRNA.matcher(t.word).find()) {
        hasMrna = true;
      }
    }
    for (Token t : e3.argument.tokens) {
      if (mRNA.matcher(t.word).find()) {
        hasMrna = true;
      }
    }
    if (hasMrna) {
      return null;
    }

    // high level
    // if (e3.comment.startsWith("# Vact Arg")) {
    // String subtext = text.substring(newTrigger.to(), newTheme.from());
    // if (subtext.contains("level")) {
    // return null;
    // }
    // }

    // promoter
    String subtext = text.substring(newTheme.to() + 1);
    if (subtext.startsWith("promoter")) {
      return null;
    }

    return e;
  }

  protected boolean belongsToOthers(Event e3) {
    // for (Event otherEvent : otherA3es) {
    // if (otherEvent.type == EventType.Transcription) {
    // if (otherEvent.trigger.equals(e3.trigger)
    // && otherEvent.argument.equals(e3.argument)) {
    // return true;
    // }
    // }
    // }
    return false;
  }

  @Override
  protected Entity getTrigger(Event e3) {

    if (e3.trigger.from() == 717) {
      // System.err.println();
    }

    if (e3.comment.startsWith("Agent Vact")) {
      return null;
    } else {
      // detect boundary
      TriggerMatcher eMatcher = new GETriggerMatcher(triggers);
      if (eMatcher.matches(e3.trigger)) {
        Entity trigger = eMatcher.getTrigger();
        // if (matchedPattern(trigger, e3.comment)) {
        // return trigger;
        // }
        return trigger;
      }
      return null;
    }
  }

  // private boolean matchedPattern(Entity trigger, String comment) {
  // Token triggerToken = trigger.tokens.get(0);
  // // find trigger
  // Trigger dstTrigger = null;
  // for (Trigger t : triggers) {
  // if (t.word.equalsIgnoreCase(triggerToken.word)
  // && t.pos.equals(triggerToken.pos)) {
  // dstTrigger = t;
  // }
  // }
  // if (dstTrigger == null) {
  // return true;
  // }
  // if (dstTrigger.patterns.isEmpty()) {
  // return true;
  // }
  //
  // // check rules;
  // Pattern p = Pattern.compile("# ([^\\[]+)");
  // Matcher m = p.matcher(comment);
  //
  // String patternName = "";
  // if (m.find()) {
  // patternName = m.group(1);
  // }
  // assert !patternName.isEmpty() : comment;
  // for (String pattern : dstTrigger.patterns) {
  // if (pattern.equals(patternName)) {
  // return true;
  // }
  // }
  //
  // return false;
  // }

  @Override
  protected List<Entity> getThemes(Event e3) {
    ThemeMatcher tMatcher = new GEThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      return tMatcher.getThemes();
    }
    return Collections.emptyList();
  }

}

class GEThemeMatcher extends ThemeMatcher {

  boolean hasMRNA;
  boolean hasTranscript;

  public GEThemeMatcher(List<Entity> a1ts, boolean isRefLink) {
    super(a1ts, isRefLink);
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

      if (Pattern.matches("(mRNA|RNA|MRNA)s?", last.word)) {
        hasMRNA = true;
      }
      if (Pattern.matches("(transcript)s?", last.word)) {
        hasTranscript = true;
      }

      String lastWordPattern = "(trans[-]activator|construct|gene|DNA|protein|product|molecule|factor|mRNA|RNA|MRNA|transcript)s?";
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

      // String lastWordPattern =
      // "(trans[-]activator|construct|gene|DNA|homodimer|protein|mutant|spec|product|heterodimer|subunit|GAS|molecule|coreceptor|mRNA|RNA|MRNA|transcript)s?$";

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
      if (theme != null && last.word.substring(midIndex + 1).equals("encoding")) {
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

    if (word_2.startsWith("transcription") && word_1.startsWith("factor")) {
      return true;
    }
    if (word_2.startsWith("fusion") && word_1.startsWith("protein")) {
      return true;
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

    if (word_2.startsWith("protein") && word_1.startsWith("complex")) {
      return true;
    }
    if (word_2.startsWith("gene") && word_1.startsWith("cluster")) {
      return true;
    }

    return false;
  }
}

class GETriggerMatcher extends TriggerMatcher {

  public GETriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());
    return b;
  }
}
