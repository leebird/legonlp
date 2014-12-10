package pengyifan.bionlpst.v2.filter;

import java.util.ArrayList;
import java.util.Arrays;
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

public class BindingFilter extends FilterOne {

  public static void main(String[] args)
      throws Exception {

    BindingFilter p = new BindingFilter();

    if (args.length != 1) {
      args = new String[] { "PMID-9442380" };
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  public BindingFilter() {
    type = EventType.Binding;
    prefix = "";
    readTrigger(0);
  }

  @Override
  protected void filter() {
    List<Event> protentialListA1 = new ArrayList<Event>();
    List<Event> protentialListProtein = new ArrayList<Event>();
    filterA3es = new ArrayList<Event>();
    for (Event e3 : a3es) {
      // trigger
      Entity trigger = getTrigger(e3);
      if (trigger == null) {
        continue;
      }
      // theme
      List<Entity> themes = getThemes(e3);
      for (Entity theme : themes) {
        // event
        Event e = getEvent(e3, trigger, theme);
        if (e != null) {
          protentialListA1.add(e);
        }
      }
      // if theme has protein
      themes = getProteinThemes(e3);
      for (Entity theme : themes) {
        // event
        Event e = getEvent(e3, trigger, theme);
        if (e != null) {
          protentialListProtein.add(e);
        }
      }
    }
    // event has two argument
    List<Event> protentialListAll = new ArrayList<Event>();
    protentialListAll.addAll(protentialListA1);
    protentialListAll.addAll(protentialListProtein);
    for (Event e : protentialListA1) {
      if (e.trigger.getFirst().word.equals("interaction")
          || e.trigger.getFirst().word.equals("interactions")
            || e.trigger.getFirst().word.startsWith("associated")) {
        for (Event e2 : protentialListAll) {
          if (e.trigger.equals(e2.trigger) && !e.argument.equals(e2.argument)) {
            filterA3es.add(e);
            break;
          }
        }
      } else {
        filterA3es.add(e);
      }
    }
  }

  @Override
  protected Entity getTrigger(Event e3) {

    if (debug && e3.trigger.from() == 495) {
      System.err.println();
    }

    // if the last word is trigger
    TriggerMatcher eMatcher = new BindingTriggerMatcher(triggers);
    if (eMatcher.matches(e3.trigger)) {
      Entity trigger = eMatcher.getTrigger();
      return trigger;
    }

    return null;
  }

  @Override
  protected List<Entity> getThemes(Event e3) {
    assert !e3.argument.tokens.isEmpty() : filename + "\n" + e3;
    // if it contains theme
    BindingThemeMatcher tMatcher = new BindingThemeMatcher(a1ts, isLink);
    if (tMatcher.matches(e3, e3.argument)) {
      List<Entity> theme = tMatcher.getThemes();
      return theme;
    }
    return Collections.emptyList();
  }

  protected List<Entity> getProteinThemes(Event e3) {
    for (Token t : e3.argument.tokens) {
      if (t.word.startsWith("protein")) {
        Entity theme = new Entity("", "protein", Collections.singletonList(t));
        return Arrays.asList(theme);
      }
      if (t.word.startsWith("NF-kappa")) {
        Entity theme = new Entity("", "protein", Collections.singletonList(t));
        return Arrays.asList(theme);
      }
      if (t.word.startsWith("DNA")) {
        Entity theme = new Entity("", "protein", Collections.singletonList(t));
        return Arrays.asList(theme);
      }
    }
    // if (e3.argument.tokens.size() == 1) {
    // return Arrays.asList(e3.argument);
    // }
    return Collections.emptyList();
  }

  @Override
  protected Event getEvent(Event e3, Entity newTrigger, Entity newTheme) {
    Event e = super.getEvent(e3, newTrigger, newTheme);
    if (e == null) {
      return null;
    }

    // boolean hasBinding = false;
    // for (Token t : newTrigger.tokens) {
    // if (t.word.equals("binding")) {
    // hasBinding = true;
    // break;
    // }
    // }
    // if (hasBinding) {
    if (isNext(
        newTrigger.to() + 1,
          "^(site|domain|genotype|activity|protein|proline)")) {
      return null;
    }

    // dna-binding
    // boolean hasDNABinding = false;
    // for (Token t : newTrigger.tokens) {
    // if (t.word.equals("DNA-binding")) {
    // hasDNABinding = true;
    // break;
    // }
    // }

    if (newTrigger.getLast().word.equals("binding")
        && isBefore(newTrigger.from(), "DNA[-]$")) {
      return e;
    }

    // not [theme]-binding
    if (newTrigger.getLast().word.equals("binding")
        && isBefore(newTrigger.from(), "[-]$")
          && newTheme.to() != newTrigger.from() - 1) {
      return null;
    }
    // -associated
    if (!newTrigger.getLast().word.equals("binding")
        && isBefore(newTrigger.from(), "[-]$")) {
      return null;
    }

    // X-associated
    boolean hasAssociate = false;
    for (Token t : newTrigger.tokens) {
      if (t.word.startsWith("associate")) {
        hasAssociate = true;
        break;
      }
    }
    if (hasAssociate) {
      if (newTrigger.getFirst().word.equalsIgnoreCase("associated")
          && e.comment.startsWith("# Arg - Vnorm")) {
        return null;
      }
    }

    // ligand
    if (newTrigger.getFirst().word.equals("ligation")
        && e3.comment.startsWith("# Vnorm with Arg")) {
      return null;
    }

    return e;
  }
}

class BindingThemeMatcher extends ThemeMatcher {

  public BindingThemeMatcher(List<Entity> a1ts, boolean isRefLink) {
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

    if (word_3.startsWith("NF-kappa")
        && word_2.startsWith("B")
          && word_1.startsWith("protein")) {
      return true;
    }

    if (!isRefLink) {
      return false;
    }

    // ref
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

    if (word_2.startsWith("tyrosine") && word_1.startsWith("kinase")) {
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

    if (word_2.startsWith("promoter") && word_1.startsWith("oligonucleotides")) {
      return true;
    }
    return false;
  }
}

class BindingTriggerMatcher extends TriggerMatcher {

  public BindingTriggerMatcher(Collection<Trigger> triggers) {
    super(triggers);
  }

  @Override
  protected boolean isTrigger(String s) {
    boolean b = Trigger.isTrigger(triggers, s.toLowerCase());
    if (!b) {
      b = s.equalsIgnoreCase("DNA-binding");
    }
    if (!b) {
      b = s.equalsIgnoreCase("DNA-bound");
    }
    if (!b) {
      b = s.equalsIgnoreCase("cross-linking");
    }
    return b;
  }
}