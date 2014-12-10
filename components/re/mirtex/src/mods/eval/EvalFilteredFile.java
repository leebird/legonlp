package mods.eval;

import java.util.ArrayList;
import java.util.Iterator;

import mods.annotation.Event;
import mods.annotation.Event.EventType;
import mods.annotation.Trigger;

public class EvalFilteredFile extends EvalFile {

  public EvalFilteredFile() {
    super();
  }

  protected boolean isEndsWith(String triggerStr) {

    String triggerStrs[] = { "interaction", "gene transcription",
        "mRNA expression", "gene expression", "proteolytic degradation" };

    for (String s : triggerStrs) {
      if (triggerStr.endsWith(s)) {
        return true;
      }
    }

    return false;
  }

  protected boolean isStartWith(String triggerStr) {
    String triggerStrs[] = { //
        // "binding analysis", //
        // // "binding activity", //
        "binding subunit",//
        // "binding complex",//
        "binding characteristic",//
        "binding propert",//
        "binding site",//
        "binding element",//
        "binding state",//
        "binding sequence",//
        "binding protein",//
        "binding region",//
        "binding domain",//
        "binding pattern",//
        "binding genotype",//
        "binding stud",//
        "DNA[-]binding domain",//
        //
        // "transcription activity",
        "transcription rate", "transcription wave",
        "transcriptional repressor", "transcriptional trans-activator",
        "transcriptional regulator", "transcriptional coactivator",
        "transcriptional level", "transcriptional mechanism",
        "transcriptional regulation", "transcriptional response",
        "transcriptional analyses", //
        //
        "expression level"//
    };

    for (String s : triggerStrs) {
      if (triggerStr.toLowerCase().startsWith(s)) {
        return true;
      }
    }

    return false;
  }

  protected void filterGold() {
    // filter gold trigger
    Iterator<Event> golditr = goldes.iterator();
    while (golditr.hasNext()) {
      Event gold = golditr.next();
      int from = gold.trigger.from();
      int to = gold.trigger.to();
      String triggerStr = text.substring(from, to);
      String triggerStr2 = text.substring(from);

      // trigger
      if (isStartWith(triggerStr2)) {
        golditr.remove();
      } else if (isEndsWith(triggerStr)) {
        ;
      } else if (equals(triggerStr, gold.type)) {
        ;
      } else {
        golditr.remove();
      }
    }
    // filter site
    for (int i = 0; i < goldes.size(); i++) {
      Event gold = goldes.get(i);
      golditr = goldes.listIterator(i + 1);
      while (golditr.hasNext()) {
        Event next = golditr.next();
        if (next.equals(gold)) {
          golditr.remove();
        }
      }
    }
  }

  @Override
  public void processFile(String dir, String filename) {

    super.processFile(dir, filename);

    if (filename.equals("PMID-8645254")) {
      System.err.println();
    }

    fp = new ArrayList<Event>();
    tp = new ArrayList<Event>();
    fn = new ArrayList<Event>();

    // filter gold
    filterGold();

    // get tp, fp, fn, tn
    if (predes.isEmpty()) {
      fn.addAll(goldes);
    } else {

      boolean predbooleans[] = new boolean[predes.size()];

      for (Event goldEvent : goldes) {
        // found is obtained from predicates
        boolean found = false;
        Event foundEvent = null;
        for (int i = 0; i < predes.size(); i++) {
          Event predEvent = predes.get(i);

          if (goldEvent.trigger.range()
              .containsRange(predEvent.trigger.range())
              && goldEvent.argument.range().containsRange(
                  predEvent.argument.range())
              && goldEvent.type == predEvent.type) {
            predbooleans[i] = true;
            found = true;
            foundEvent = predEvent;
          }
        }
        if (found) {
          tp.add(new Event(goldEvent.filename, goldEvent.type,
              goldEvent.trigger, goldEvent.argumentType, goldEvent.argument, foundEvent.comment));
        } else {
          fn.add(goldEvent);
        }
      }
      for (int i = 0; i < predbooleans.length; i++) {
        if (predbooleans[i]) {
          continue;
        }
        Event predEvent = predes.get(i);

        fp.add(new Event(predEvent));
        for (int j = i + 1; j < predbooleans.length; j++) {
          Event nextEvent = predes.get(j);

          if (predEvent.trigger.range().isOverlappedBy(
              nextEvent.trigger.range())
              && predEvent.argument.range().isOverlappedBy(
                  nextEvent.argument.range())
              && predEvent.type == nextEvent.type) {
            predbooleans[j] = true;
          }
        }
      }
    }
  }

  protected boolean equals(String triggerStr, EventType type) {
    for (Trigger t : triggers) {
      if (t.word.equalsIgnoreCase(triggerStr) && t.type == type) {
        return true;
      }
    }
    return false;
  }

}
