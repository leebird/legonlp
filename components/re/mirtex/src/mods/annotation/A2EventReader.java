package mods.annotation;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import mods.annotation.Event.ArgumentType;
import mods.annotation.Event.EventType;

public class A2EventReader implements Closeable {

  public static List<Event> readEvents(String a1filename, String a2filename,
      List<Entity> entities) {

    File f = new File(a2filename);
    if (!f.exists()) {
      return Collections.emptyList();
    }

    List<Event> es = new ArrayList<Event>();
    try {
      A2EventReader reader = new A2EventReader(a1filename, a2filename, entities);
      es.addAll(reader.readEvents());
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("no such file: " + a2filename);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("cannot read: " + a2filename);
      System.exit(1);
    }
    return es;
  }

  LineNumberReader       reader;
  String                 basename;
  List<Entity>           entities;
  List<A1A2Entity>       a1a2entities;
  String                 a1filename;
  String                 a2filename;

  private static Pattern p1 = Pattern
                                .compile("(T\\d+)\\t(\\w+) (\\d+) (\\d+)\\t(.*)");

  /**
   * 
   * @param a1filename entity file
   * @param a2filename entity + event file
   * @param entities
   * @throws FileNotFoundException
   */
  public A2EventReader(
      String a1filename,
      String a2filename,
      List<Entity> entities)
      throws FileNotFoundException {
    basename = FilenameUtils.getBaseName(a2filename);
    this.a1filename = a1filename;
    this.a2filename = a2filename;
    reader = new LineNumberReader(new FileReader(a2filename));
    this.entities = entities;
  }

  @Override
  public void close()
      throws IOException {
    reader.close();
  }

  public List<Event> readEvents()
      throws IOException {

    a1a2entities = readA1A2Entity(a1filename);
    a1a2entities.addAll(readA1A2Entity(a2filename));

    List<Event> es = new ArrayList<Event>();
    List<Set<Entity>> equivs = new ArrayList<Set<Entity>>();
    String line = null;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("E")) {
        es.addAll(valueOfA2Events(line));
      } else if (line.startsWith("*")) {
        equivs.add(valueOfA2Equiv(line));
      }
    }
    // handle Equiv
    List<Event> equivEvents = new ArrayList<Event>();
    for (Event e : es) {
      for (Set<Entity> set : equivs) {
        if (set.contains(e.argument)) {
          for (Entity arg : set) {
            if (arg != e.argument) {
              equivEvents.add(new Event(e.filename, e.type, e.trigger,
                  e.argumentType, arg, e.comment));
            }
          }
        }
      }
    }
    es.addAll(equivEvents);
    return es;
  }

  private Set<Entity> valueOfA2Equiv(String str) {
    Set<Entity> set = new HashSet<Entity>();
    String toks[] = str.split("\\s+");
    for (int i = 2; i < toks.length; i++) {
      Entity e = find(toks[i]);
      assert e != null;
      set.add(e);
    }
    return set;
  }

  private List<Event> valueOfA2Events(String str) {

    String toks[] = str.split("\\s+");

    String s = toks[1];

    String typeStr = s.substring(0, s.indexOf(':'));
    if (typeStr.equals("Protein_modification")
        || typeStr.equals("Ubiquitination")
        || typeStr.equals("Acetylation")
        || typeStr.equals("Deacetylation")) {
      return Collections.emptyList();
    }

    EventType type = EventType.getEnum(s.substring(0, s.indexOf(':')));
    Entity trigger = find(s.substring(s.indexOf(':') + 1));
    s = toks[2];

    LinkedList<Event> bs = new LinkedList<Event>();

    ArgumentType argumentType = ArgumentType.getEnum(s.substring(
        0,
        s.indexOf(':')));
    String argumentId = s.substring(s.indexOf(':') + 1);
    if (!argumentId.startsWith("E")) {
      bs.add(new Event(basename, type, trigger, argumentType, find(argumentId),
          ""));
    }

    // other themes or sites
    for (int i = 3; i < toks.length; i++) {
      s = toks[i];
      argumentType = ArgumentType.getEnum(s.substring(0, s.indexOf(':')));

      argumentId = s.substring(s.indexOf(':') + 1);
      if (!argumentId.startsWith("E")) {
        bs.add(new Event(basename, type, trigger, argumentType,
            find(argumentId), ""));
      }
    }
    return bs;
  }

  private Entity find(String id) {
    A1A2Entity a1a2Entity = null;
    for (A1A2Entity e : a1a2entities) {
      if (e.id().equals(id)) {
        a1a2Entity = e;
        break;
      }
    }
    assert a1a2Entity != null : id;
    for (Entity e : entities) {
      if (e.range().equals(a1a2Entity.range())) {
        return e;
      }
    }
    assert false : basename + ": " + id;
    return null;
  }

  private List<A1A2Entity> readA1A2Entity(String filename) {
    File f = new File(filename);
    List<A1A2Entity> ts = new ArrayList<A1A2Entity>();
    try {
      LineNumberReader reader = new LineNumberReader(new FileReader(f));
      String line = null;
      while ((line = reader.readLine()) != null) {
        Matcher m = p1.matcher(line.trim());
        if (m.find()) {
          A1A2Entity e = new A1A2Entity(m.group(1),
              Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
          ts.add(e);
        }
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("no such file: " + filename);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("cannot read: " + filename);
      System.exit(1);
    }
    return ts;
  }
}
