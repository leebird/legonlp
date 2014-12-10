package mods.annotation;

import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import mods.annotation.Event.EventType;

public class TriggerReader implements Closeable {

  static List<Trigger> triggers = null;

  public static List<Trigger> readTriggers(String filename) {
    triggers = new LinkedList<Trigger>();
    try {
      TriggerReader reader = new TriggerReader(new FileReader(filename));
      Trigger t = null;
      while ((t = reader.readTrigger()) != null) {
        triggers.add(t);
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    return triggers;
  }

  public static Collection<Trigger> readTriggers(boolean skip) {
    if (skip) {
      return CollectionUtils.select(triggers, new Predicate<Trigger>() {

        @Override
        public boolean evaluate(Trigger t) {
          return !t.mark.equals("-") && !t.mark.equals("?");
        }
      });
    }
    return triggers;
  }

  public static Collection<Trigger> readTriggers(String filename, boolean skip) {
    triggers = readTriggers(filename);
    return readTriggers(skip);
  }

  public static
      Collection<Trigger>
      readTriggers(String filename, EventType type) {
    triggers = readTriggers(filename);
    return readTriggers(type);
  }

  public static Collection<Trigger> readTriggers(EventType type) {
    class TypePredicate implements Predicate<Trigger> {

      final EventType type;

      TypePredicate(EventType type) {
        this.type = type;
      }

      @Override
      public boolean evaluate(Trigger t) {
        return t.type == type;
      }

    }
    return CollectionUtils.select(triggers, new TypePredicate(type));
  }

  public static Collection<Trigger> readTriggers(String filename,
      EventType type, String pos, int freq) {
    triggers = readTriggers(filename);
    return readTriggers(type, pos, freq);
  }

  public static Collection<Trigger> readTriggers(EventType type, String pos,
      int freq) {
    class TypePredicate implements Predicate<Trigger> {

      final EventType type;
      final String    pos;
      final int       freq;

      TypePredicate(EventType type, String pos, int freq) {
        this.type = type;
        this.pos = pos;
        this.freq = freq;
      }

      @Override
      public boolean evaluate(Trigger t) {
        return t.type == type
            && t.pos.equalsIgnoreCase(pos)
            && t.freq >= freq
            && !t.mark.equals("-")
            && !t.mark.equals("?");
      }

    }
    return CollectionUtils.select(triggers, new TypePredicate(type, pos, freq));
  }

  private final LineNumberReader reader;

  public TriggerReader(Reader in) {
    reader = new LineNumberReader(in);
  }

  @Override
  public void close()
      throws IOException {
    reader.close();
  }

  public Trigger readTrigger()
      throws IOException {
    String line = reader.readLine();
    if (line == null) {
      return null;
    }
    line = line.trim();
    while (skip(line)) {
      line = reader.readLine();
      if (line == null) {
        return null;
      }
      line = line.trim();
    }
    String toks[] = line.split("\\t");
    assert toks.length >= 6 : reader.getLineNumber() + ": " + line;
    try {
      Trigger t = new Trigger();
      t.type = Event.EventType.valueOf(toks[0]);
      t.word = toks[1];
      t.lexeme = toks[2];
      t.pos = toks[3];
      t.freq = Integer.parseInt(toks[4]);
      t.mark = toks[5];
            
      if (toks.length >= 7) {
        String patterns[] = toks[6].split(",");
        for (String p : patterns) {
          if (!p.isEmpty()) {
            t.patterns.add(p);
          }
        }
      }
      return t;
    } catch (Exception e) {
      System.err.println("can not parse line "
          + reader.getLineNumber()
          + ": "
          + line);
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  private boolean skip(String line) {
    if (line.isEmpty() || line.startsWith("//")) {
      return true;
    }
    return false;
  }

}
