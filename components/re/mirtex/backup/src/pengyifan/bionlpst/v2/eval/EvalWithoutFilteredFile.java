package pengyifan.bionlpst.v2.eval;

import java.util.ArrayList;
import java.util.Iterator;

import pengyifan.bionlpst.v2.annotation.Event;

public class EvalWithoutFilteredFile extends EvalFile {

  @Override
  public final void processFile(String dir, String filename) {

    super.processFile(dir, filename);

    if (filename.equals("PMID-10225377")) {
      // System.err.println();
    }

    fp = new ArrayList<Event>();
    tp = new ArrayList<Event>();
    fn = new ArrayList<Event>();

    // filter gold trigger
    Iterator<Event> golditr = goldes.iterator();
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
}
