package pengyifan.bionlp.v3.bionex;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.A2EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;

public class SplitBioNex extends FileProcessor {

  public static void main(String args[]) {
    FileProcessor fileProcessor = new SplitBioNex();
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }

  List<Event> events;

  @Override
  protected void readResource(String dir, String filename) {

    super.filename = filename;
    String a2filename = dir + "/bionex/" + filename + ".a2";

    try {
      List<Entity> ts = A1EntityReader.readEntities(a2filename);
      ts.addAll(A1EntityReader.readEntities(a2filename));
      events = A2EventReader.readEvents(a2filename, a2filename, ts);
    } catch (IllegalArgumentException e) {
      System.err.println(a2filename);
      System.err.println(e.getMessage() + "\n");
    } catch (IllegalStateException e) {
      System.err.println(a2filename);
      System.err.println(e.getMessage() + "\n");
    }
  }

  @Override
  public final void processFile(String dir, String filename) {

    // System.out.println(filename);
    super.processFile(dir, filename);

    List<Event> coevents = new ArrayList<Event>();
    List<Event> pwevents = new ArrayList<Event>();
    List<Event> mcevents = new ArrayList<Event>();

    for (Event e : events) {
      if (e.type == EventType.PartWhole) {
        pwevents.add(e);
      } else if (e.type == EventType.MemberCollection) {
        mcevents.add(e);
      } else if (e.type == EventType.Coref) {
        coevents.add(e);
      } else {
        assert false : filename + ": " + e;
      }
    }

    // print
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/bionex/"
          + filename
          + ".part"));
      for (Event e : pwevents) {
        out.println(e);
      }
      out.close();

      out = new PrintStream(new FileOutputStream(dir
          + "/bionex/"
          + filename
          + ".member"));
      for (Event e : mcevents) {
        out.println(e);
      }
      out.close();

      out = new PrintStream(new FileOutputStream(dir
          + "/bionex/"
          + filename
          + ".co"));
      for (Event e : coevents) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

  }
}