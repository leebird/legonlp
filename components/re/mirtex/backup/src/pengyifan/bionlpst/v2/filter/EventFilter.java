package pengyifan.bionlpst.v2.filter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A3EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;

public class EventFilter extends FileProcessor {

  /**
   * @param args
   */
  public static void main(String[] args) {
    EventFilter p = new EventFilter();

    // args = new String[] { "PMID-10202027" };

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  protected String       text;
  protected List<Entity> a1ts;
  protected List<Event>  a4es;
  protected List<Event>  filteredA4es;

  @Override
  protected void readResource(String dir, String filename) {
    // read text
    text = Utils.readText(dir + "/" + filename + ".txt");
    a4es = A3EventReader.readEvents(dir + "/" + filename + ".a4");
  }

  @Override
  public void processFile(String dir, String filename) {
    super.processFile(dir, filename);
    filter();
    printA(dir, filename, "a6");
  }

  protected void filter() {
    filteredA4es = new ArrayList<Event>(a4es);
    // boolean removed[] = new boolean[a4es.size()];
    // Arrays.fill(removed, false);
    //
    // for (int i = 0; i < a4es.size(); i++) {
    // Event e1 = a4es.get(i);
    // Token t1 = e1.trigger.tokens.get(0);
    // for (int j = i + 1; j < a4es.size(); j++) {
    // Event e2 = a4es.get(j);
    // Token t2 = e2.trigger.tokens.get(0);
    //
    // if (e1.argument.equals(e2.argument)
    // && e1.trigger.equals(e2.trigger)
    // && t1.word.startsWith("express")) {
    // if (e1.type == EventType.Gene_expression
    // && e2.type != EventType.Gene_expression) {
    // removed[j] = true;
    // } else if (e1.type != EventType.Gene_expression
    // && e2.type == EventType.Gene_expression) {
    // removed[i] = true;
    // }
    // }
    //
    // if (e1.argument.equals(e2.argument)) {
    // // associat
    // if (t1.word.startsWith("associat") || t1.word.startsWith("Associat")) {
    // removed[i] = true;
    // } else if (t2.word.startsWith("associat")
    // || t2.word.startsWith("Associat")) {
    // removed[j] = true;
    // }
    // // activity
    // if (t1.word.startsWith("activit")) {
    // removed[i] = true;
    // } else if (t2.word.startsWith("activit")) {
    // removed[j] = true;
    // }
    // // receptor
    // if (t1.word.startsWith("receptor")) {
    // removed[i] = true;
    // } else if (t2.word.startsWith("receptor")) {
    // removed[j] = true;
    // }
    //
    // // both GE
    // if (e1.type == EventType.Gene_expression
    // && e2.type == EventType.Gene_expression) {
    // if (t1.word.startsWith("express")) {
    // removed[j] = true;
    // } else if (t2.word.startsWith("express")) {
    // removed[i] = true;
    // }
    // }
    //
    // // both binding
    // if (e1.type == EventType.Binding && e2.type == EventType.Binding) {
    // if (t1.word.startsWith("bind") || t1.word.startsWith("bound")) {
    // removed[j] = true;
    // } else if (t2.word.startsWith("bind")
    // || t2.word.startsWith("bound")) {
    // removed[i] = true;
    // }
    // }
    // }
    // }
    // }
    //
    // for (int i = 0; i < a4es.size(); i++) {
    // if (!removed[i]) {
    // filteredA4es.add(a4es.get(i));
    // }
    // }
  }

  protected void printA(String dir, String filename, String ext) {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/"
          + filename
          + "."
          + ext));
      for (Event e : filteredA4es) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
