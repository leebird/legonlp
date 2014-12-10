package pengyifan.bionlpst.v3.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.A2EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;

public class TriggerTest extends BatchProcessor {

  public static void main(String args[]) {
    FileProcessor fileProcessor = new TriggerTestFile();
    BatchProcessor batchProcessor = new TriggerTest(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }

  Map<String, Integer> map;

  public TriggerTest(FileProcessor fileProcessor) {
    super(fileProcessor);
    map = new HashMap<String, Integer>();
  }

  @Override
  public void processDir(String dir) {
    for (File f : allFiles(dir)) {
      String filename = f.getName();
      filename = filename.substring(0, filename.lastIndexOf('.'));
      fileProcessor.processFile(dir, filename);

      TriggerTestFile t = (TriggerTestFile) fileProcessor;
      for (String s : t.triggers) {
        if (map.containsKey(s)) {
          map.put(s, map.get(s) + 1);
        } else {
          map.put(s, 1);
        }
      }
    }

    // sort
    List<Map.Entry<String, Integer>> es = new ArrayList<Map.Entry<String, Integer>>(
        map.entrySet());
    Collections.sort(es, new Comparator<Map.Entry<String, Integer>>() {

      @Override
      public int compare(Entry<String, Integer> arg0,
          Entry<String, Integer> arg1) {
        return new Integer(arg1.getValue()).compareTo(arg0.getValue());
      }
    });

    EventType types[] = new EventType[] { EventType.Gene_expression,
        EventType.Transcription, EventType.Phosphorylation,
        EventType.Protein_catabolism, EventType.Localization, EventType.Binding };
    for (EventType t : types) {
      for (Map.Entry<String, Integer> e : es) {
        if (e.getKey().startsWith(t.toString())) {
          System.out.println(e);
        }
      }
    }
  }
}

class TriggerTestFile extends FileProcessor {

  List<String> triggers;
  String       text;
  List<Event>  events;

  @Override
  protected void readResource(String dir, String filename) {
    text = Utils.readText(dir + "/" + filename + ".txt");

    String a1filename = dir + "/" + filename + ".a1";
    String a2filename = dir + "/" + filename + ".a2";

    List<Entity> ts = A1EntityReader.readEntities(a1filename);
    ts.addAll(A1EntityReader.readEntities(a2filename));

    events = A2EventReader.readEvents(a1filename, a2filename, ts);
  }

  @Override
  public final void processFile(String dir, String filename) {

    // System.out.println(filename);
    super.processFile(dir, filename);

    triggers = new LinkedList<String>();
    for (Event e : events) {
      String triggerStr = text.substring(e.trigger.from(), e.trigger.to())
          .toLowerCase();
      triggers.add(e.type + "\t" + triggerStr);
    }
  }
}