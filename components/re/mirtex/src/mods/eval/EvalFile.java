package mods.eval;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import mods.annotation.A1EntityReader;
import mods.annotation.A3EventReader;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Trigger;
import mods.annotation.TriggerReader;

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
