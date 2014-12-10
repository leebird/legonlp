package pengyifan.bionlpst.v2.eval;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.A3EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Trigger;
import pengyifan.bionlpst.v2.annotation.TriggerReader;

public abstract class EvalFile extends FileProcessor {

  public List<Event>      fp;
  public List<Event>      tp;
  public List<Event>      fn;
  protected String        text;
  protected List<Event>   predes;
  protected List<Event>   goldes;
  protected Collection<Trigger> triggers;

  public EvalFile() {
    triggers = TriggerReader.readTriggers(Env.TRIGGER, true);
  }

  @Override
  protected void readResource(String dir, String filename) {

    String a1filename = dir + "/" + filename + ".a1";
    String a2filename = dir + "/" + filename + ".a2";
    String gldfilename = dir + "/" + filename + ".gld";

    // read text
    text = Utils.readText(dir + "/" + filename + ".txt");

    // read gold
    List<Entity> a1ts = A1EntityReader.readEntities(a1filename);
    List<Entity> a2ts = A1EntityReader.readEntities(a2filename);
    List<Entity> a1a2ts = new LinkedList<Entity>();
    a1a2ts.addAll(a1ts);
    a1a2ts.addAll(a2ts);
    goldes = A3EventReader.readEvents(gldfilename);

    // read extracted
    predes = A3EventReader.readEvents(dir + "/" + filename + ".a6");
  }
}
