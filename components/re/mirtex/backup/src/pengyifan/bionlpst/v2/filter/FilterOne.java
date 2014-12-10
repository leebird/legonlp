package pengyifan.bionlpst.v2.filter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.A3EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.ArgumentType;
import pengyifan.bionlpst.v2.annotation.Event.EventType;
import pengyifan.bionlpst.v2.annotation.Trigger;
import pengyifan.bionlpst.v2.annotation.TriggerReader;

public abstract class FilterOne extends FileProcessor {

  EventType                     type;
  String                        prefix;

  protected String              text;
  protected List<Entity>        a1ts;

  protected List<Event>         a3es;
  protected List<Event>         a5es;
  protected List<Event>         filterA3es;

  protected Collection<Trigger> nnTriggers;
  protected Collection<Trigger> nnsTriggers;
  protected Collection<Trigger> jjTriggers;
  protected Collection<Trigger> vbTriggers;
  protected Collection<Trigger> vbzTriggers;
  protected Collection<Trigger> vbdTriggers;
  protected Collection<Trigger> vbgTriggers;
  protected Collection<Trigger> vbnTriggers;
  protected Collection<Trigger> vbpTriggers;
  protected Collection<Trigger> triggers;

  protected boolean             isLink;

  FilterOne() {
    prefix = "";
    isLink = true;
  }

  @Override
  public void processFile(String dir, String filename) {
    // System.out.println(filename);
    super.processFile(dir, filename);
    filter();
    printA(dir, filename, "a3." + type);
  }

  @Override
  protected void readResource(String dir, String filename) {
    text = Utils.readText(dir + "/" + filename + ".txt");
    a1ts = A1EntityReader.readEntities(dir + "/" + filename + ".a1");

    a3es = new ArrayList<Event>();
    a3es.addAll(A3EventReader.readEvents(dir + "/" + filename + ".a3"));

    // link
    if (isLink) {
      a3es.addAll(A3EventReader.readEvents(dir + "/" + filename + ".a7"));
    }
    
    triggers = new LinkedList<Trigger>();
    triggers.addAll(nnTriggers);
    triggers.addAll(nnsTriggers);
    triggers.addAll(jjTriggers);
    triggers.addAll(vbTriggers);
    triggers.addAll(vbzTriggers);
    triggers.addAll(vbdTriggers);
    triggers.addAll(vbgTriggers);
    triggers.addAll(vbnTriggers);
    triggers.addAll(vbpTriggers);
  }
    
  protected void readTrigger(int freq) {
    nnTriggers = TriggerReader.readTriggers(Env.TRIGGER, type, "NN", freq);
    nnsTriggers = TriggerReader.readTriggers(type, "NNS", freq);
    jjTriggers = TriggerReader.readTriggers(type, "JJ", freq);
    vbTriggers = TriggerReader.readTriggers(type, "VB", freq);
    vbgTriggers = TriggerReader.readTriggers(type, "VBG", freq);
    vbnTriggers = TriggerReader.readTriggers(type, "VBN", freq);
    vbzTriggers = TriggerReader.readTriggers(type, "VBZ", freq);
    vbdTriggers = TriggerReader.readTriggers(type, "VBD", freq);
    vbpTriggers = TriggerReader.readTriggers(type, "VBP", freq);
  }

  protected void filter() {
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
          filterA3es.add(e);
        }
      }
    }
  }

  protected Event getEvent(Event e3, Entity newTrigger, Entity newTheme) {
    // trigger and theme can not be overlapped
    if (newTrigger.range().isOverlappedBy(newTheme.range())) {
      return null;
    }
    return new Event(e3.filename, type, newTrigger, ArgumentType.Theme, newTheme, e3.comment);
  }

  protected abstract List<Entity> getThemes(Event e3);

  protected abstract Entity getTrigger(Event e3);

  protected void printA(String dir, String filename, String ext) {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/"
          + filename
          + "."
          + ext));
      for (Event e : filterA3es) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  protected
      List<Entity>
      findAnotherEntityInCo(Entity argument, List<Event> coes) {
    List<Entity> cos = new ArrayList<Entity>();
    for (Event e : coes) {
      if (e.trigger.equals(argument)) {
        cos.add(e.argument);
      }
      if (e.argument.equals(argument)) {
        cos.add(e.trigger);
      }
    }
    return cos;
  }

  /**
   * [toIndex , ...
   * 
   * @param toIndex
   * @param prefix
   * @return
   */
  protected boolean isNext(int toIndex, String prefix) {
    if (toIndex > text.length()) {
      return false;
    }
    String subtext = text.substring(toIndex);
    Pattern p = Pattern.compile(prefix, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(subtext);
    return m.find();
  }

  /**
   * ... , fromIndex-1]
   * 
   * @param fromIndex
   * @param suffix
   * @return
   */
  protected boolean isBefore(int fromIndex, String suffix) {
    if (fromIndex <= 0) {
      return false;
    }
    String subtext = text.substring(0, fromIndex);
    Pattern p = Pattern.compile(suffix, Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(subtext);
    return m.find();
  }
}
