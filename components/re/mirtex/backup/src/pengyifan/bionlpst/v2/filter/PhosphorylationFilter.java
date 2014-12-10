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

public class PhosphorylationFilter extends FilterOne {

  public static void main(String[] args)
      throws Exception {

    PhosphorylationFilter p = new PhosphorylationFilter();

    if (args.length != 1) {
      args = new String[] { "PMC-2065877-04-Results-03" };
      p.debug = true;
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  public PhosphorylationFilter() {
    type = EventType.Phosphorylation;
    prefix = "";
    readTrigger(0);
  }

  @Override
  protected Entity getTrigger(Event e3) {
    if (debug && e3.trigger.from() == 847) {
      // System.err.println();
    }
    if (e3.comment.startsWith("# Arg Vact")
        || e3.comment.startsWith("# Arg does something by Vvbg")) {
      return null;
    } else {
      // detect boundary
      TriggerMatcher eMatcher = new PhosTriggerMatcher(triggers);
      if (eMatcher.matches(e3.trigger)) {
        return eMatcher.getTrigger();
      }
      return null;
    }
  }

  @Override
  protected List<Entity> getThemes(Event e3) {
    ThemeMatcher tMatcher = new PhosThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      return tMatcher.getThemes();
    }
    return Collections.emptyList();
  }

}

class PhosThemeMatcher extends ThemeMatcher {

  public PhosThemeMatcher(List<Entity> a1ts, boolean isRafLink) {
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

        lastWordPattern = "(TRE|SRE|species|TSS|homodimer|mutant|spec|heterodimer|subunit|GAS|coreceptor|pathway|ligand)s?";
        if (Pattern.matches(lastWordPattern, last.word)
            && tokens.size() - 2 >= 0) {
          last = tokens.get(tokens.size() - 2);
        }
      }

      // String lastWordPattern =
      // "(TRE|SRE|species|TSS|gene|DNA|homodimer|protein|mutant|spec|product|heterodimer|subunit|GAS|molecule|coreceptor|factor|pathway|ligand)s?";

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

    // last word xxx(xxx)
    // PMID-9442380
    if (themes.size() == 1) {
      Token firstToken = new Token("", "NN", last.from(), themes.get(0).to());
      if (firstToken.from() < firstToken.to()) {
        firstToken = new Token(last.word.substring(0, firstToken.to()
            - firstToken.from()), firstToken.pos, firstToken.from(),
            firstToken.to());
        theme = endsWith(firstToken);
        if (theme != null && !themes.isEmpty()) {
          themes.add(theme);
        }
      }
    }

    // last word xxx/xxx
    int midIndex = last.word.indexOf('/');
    if (midIndex != -1) {
      Token firstToken = new Token(last.word.substring(0, midIndex), "NN",
          last.from(), last.from() + midIndex);
      theme = endsWith(firstToken);
      if (theme != null && !themes.isEmpty()) {
        themes.add(theme);
      }

      // last word xxx/xxx/xxx
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

    midIndex = last.word.indexOf('-');
    if (midIndex != -1) {
      Token firstToken = new Token(last.word.substring(0, midIndex), "NN",
          last.from(), last.from() + midIndex);
      theme = endsWith(firstToken);
      if (theme != null && !themes.isEmpty()) {
        themes.add(theme);
      }
    }

    midIndex = last.word.indexOf('.');
    if (midIndex != -1) {
      Token firstToken = new Token(last.word.substring(0, midIndex), "NN",
          last.from(), last.from() + midIndex);
      theme = endsWith(firstToken);
      if (theme != null && !themes.isEmpty()) {
        themes.add(theme);
      }
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

class PhosTriggerMatcher extends TriggerMatcher {

  public PhosTriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());
    if (!b) {
      if (s.endsWith("-phosphorylated")) {
        b = true;
      }
    }
    return b;
  }
}
