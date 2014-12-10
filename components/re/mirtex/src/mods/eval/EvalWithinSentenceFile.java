package mods.eval;

import java.util.Iterator;

import mods.annotation.Event;

public class EvalWithinSentenceFile extends EvalFilteredFile {

  @Override
  protected void filterGold() {
    
    super.filterGold();
    
    Iterator<Event> golditr = goldes.iterator();
    while (golditr.hasNext()) {
      Event gold = golditr.next();
      int from = gold.trigger.from();
      int to = gold.trigger.to();

      // within sentence
      from = gold.trigger.to();
      to = gold.argument.from();
      if (from > to) {
        from = gold.argument.to();
        to = gold.trigger.from();
      }
      if (text.substring(from, to).contains(". ")) {
        golditr.remove();
      }
    }
  }

}
