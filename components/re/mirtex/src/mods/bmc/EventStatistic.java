package mods.bmc;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mods.utils.BatchProcessor;
import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import mods.annotation.A1EntityReader;
import mods.annotation.A2EventReader;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Event.EventType;

public class EventStatistic extends BatchProcessor {



  public static void main(String args[]) {
    EventStatistic e = new EventStatistic(new EventStatisticFile());

    EventType types[] = new EventType[] { EventType.Gene_expression,
        EventType.Transcription, EventType.Protein_catabolism,
        EventType.Phosphorylation, EventType.Localization, EventType.Binding };
    
    e.map.clear();
    e.processDir(Env.basedir + "/BioNLP-ST_2011_genia_train_data_rev1");
    for (EventType t : types) {
      System.out.printf("%s\t%d\n", t, e.map.get(t));
    }
    
    System.out.println();

    e.map.clear();
    e.processDir(Env.basedir + "/BioNLP-ST_2011_genia_devel_data_rev1");
    for (EventType t : types) {
      System.out.printf("%s\t%d\n", t, e.map.get(t));
    }
  }

  Map<EventType, Integer> map;
  
  public EventStatistic(FileProcessor fileProcessor) {
    super(fileProcessor);
    map = new HashMap<EventType, Integer>();
  }

  @Override
  public void processDir(String dir) {
    for (File f : allFiles(dir)) {
      String filename = f.getName();
      filename = filename.substring(0, filename.lastIndexOf('.'));
      fileProcessor.processFile(dir, filename);
      EventStatisticFile p = (EventStatisticFile)fileProcessor;
      for(EventType type: p.map.keySet()) {
        if (map.containsKey(type)) {
          map.put(type, map.get(type) + p.map.get(type));
        } else {
          map.put(type, p.map.get(type));
        }
      }
    }
  }

}

class EventStatisticFile extends FileProcessor {

  EventStatisticFile() {
    map = new HashMap<EventType, Integer>();
  }

  PrintStream             out = System.out;
  Map<EventType, Integer> map;
  String                  text;
  List<Entity>            a1ts, a2ts;
  List<Event>             a2es;

  @Override
  protected void readResource(String dir, String filename) {
    this.filename = filename;

    String a1filename = dir + "/" + filename + ".a1";
    String a2filename = dir + "/" + filename + ".a2";

    // read text
    text = Utils.readText(dir + "/" + filename + ".txt");
    // read entity
    a1ts = A1EntityReader.readEntities(a1filename);
    a2ts = A1EntityReader.readEntities(a2filename);

    // read event
    List<Entity> a1a2ts = new LinkedList<Entity>();
    a1a2ts.addAll(a1ts);
    a1a2ts.addAll(a2ts);
    a2es = A2EventReader.readEvents(a1filename, a2filename, a1a2ts);
  }

  @Override
  public void processFile(String dir, String filename) {
    // System.out.println(filename);
    readResource(dir, filename);
    map.clear();
    
    // remove
    LinkedList<Event> list = new LinkedList<Event>(a2es);

    for (int i = 0; i < list.size(); i++) {
      Event e = list.get(i);
      Iterator<Event> itr = list.listIterator(i + 1);
      while (itr.hasNext()) {
        Event e2 = itr.next();
        if (e.type == e2.type
            && e.trigger.equals(e2.trigger)
            && e.argument.equals(e2.argument)) {
          itr.remove();
        }
      }
    }

    for (Event e : list) {
      if (map.containsKey(e.type)) {
        map.put(e.type, map.get(e.type) + 1);
      } else {
        map.put(e.type, 1);
      }
    }
  }
}
