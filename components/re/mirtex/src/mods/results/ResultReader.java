package mods.results;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mods.annotation.A3EventReader;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Event.ArgumentType;
import mods.annotation.Event.EventType;

public class ResultReader implements Closeable {

  static final Pattern     p = Pattern.compile("(\\w+):(.*)");

  private final LineNumberReader reader;

  public ResultReader(InputStream in) {
    reader = new LineNumberReader(new InputStreamReader(in));
  }

  public List<Event> readResults()
      throws NumberFormatException, IOException {

    List<Event> events = new ArrayList<Event>();

    String line = null;

    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }
      String toks[] = line.split("\t");

      String efilename = toks[0];
      EventType type = EventType.valueOf(toks[2]);

      // trigger
      Matcher m = p.matcher(toks[3]);
      m.find();
      Entity trigger = new Entity("", m.group(1), A3EventReader.parseTokens(m
          .group(2)));

      // argument
      m = p.matcher(toks[4]);
      m.find();
      Entity argument = new Entity("", m.group(1), A3EventReader.parseTokens(m
          .group(2)));

      String comment = "";
      // comment
      if (toks.length == 6) {
        comment = toks[5];
      }

      Event e = new Event(efilename, type, trigger, ArgumentType.Theme, argument, comment);
      events.add(e);
    }

    return events;
  }

  @Override
  public void close()
      throws IOException {
    reader.close();
  }
}
