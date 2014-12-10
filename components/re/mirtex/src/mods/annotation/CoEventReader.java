package mods.annotation;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import mods.annotation.Event.ArgumentType;
import mods.annotation.Event.EventType;

public class CoEventReader implements Closeable {

  public static
      List<Event>
      readEvents(String cofilename, List<Entity> entities) {

    File f = new File(cofilename);
    if (!f.exists()) {
      return Collections.<Event> emptyList();
    }

    List<Event> es = new ArrayList<Event>();
    try {
      CoEventReader reader = new CoEventReader(cofilename, entities);
      es.addAll(reader.readEvents());
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("no such file: " + cofilename);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("cannot read: " + cofilename);
      System.exit(1);
    }
    return es;
  }

  LineNumberReader       reader;
  String                 basename;
  List<Entity>           entities;
  List<A1A2Entity>       coentities;
  String                 cofilename;

  private static Pattern p1 = Pattern
                                .compile("(T\\d+)\\t(\\w+) (\\d+) (\\d+)\\t([^\\t]+)");

  /**
   * 
   * @param a1filename entity file
   * @param a2filename entity + event file
   * @param entities
   * @throws FileNotFoundException
   */
  public CoEventReader(String cofilename, List<Entity> entities)
      throws FileNotFoundException {
    basename = FilenameUtils.getBaseName(cofilename);
    this.cofilename = cofilename;
    reader = new LineNumberReader(new FileReader(cofilename));
    this.entities = entities;
  }

  @Override
  public void close()
      throws IOException {
    reader.close();
  }

  public List<Event> readEvents()
      throws IOException {

    coentities = readA1A2Entity(cofilename);

    List<Event> es = new ArrayList<Event>();
    String line = null;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("R")) {
        es.add(valueOfCoEvent(line));
      }
    }
    return es;
  }

  private Event valueOfCoEvent(String str) {

    String toks[] = str.split("\\s+");

    EventType type = EventType.valueOf(toks[1]);

    String s = toks[2];
    Entity trigger = find(s.substring(s.indexOf(':') + 1));
    s = toks[3];
    Entity argument = find(s.substring(s.indexOf(':') + 1));

    return new Event(basename, type, trigger, ArgumentType.Coreference, argument, "# link");
  }

  private Entity find(String id) {
    A1A2Entity a1a2Entity = null;
    for (A1A2Entity e : coentities) {
      if (e.id().equals(id)) {
        a1a2Entity = e;
        break;
      }
    }
    assert a1a2Entity != null;
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
        line = line.trim();
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
