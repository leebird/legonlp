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

public class A3EventReader implements Closeable {

  public static List<Event> readEvents(String filename) {

    File f = new File(filename);
    if (!f.exists()) {
      return Collections.emptyList();
    }

    List<Event> es = new ArrayList<Event>();

    try {
      A3EventReader reader = new A3EventReader(filename);
      Event e = null;
      while ((e = reader.readEvent()) != null) {
        es.add(e);
      }
      //System.out.println(es);
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("no such file: " + filename);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("cannot read: " + filename);
      System.exit(1);
    }
    return es;
  }

  LineNumberReader       reader;
  String                 basename;
  List<Entity>           entities;

  private static Pattern p3 = Pattern
                                .compile("E\\t(\\w+)\\tTrigger:([^\\t]+)\\t(\\w+):([^\\t]+)\\t(.*)");

  public A3EventReader(String filename)
      throws FileNotFoundException {
    basename = FilenameUtils.getBaseName(filename);
    reader = new LineNumberReader(new FileReader(filename));
  }

  public Event readEvent()
      throws IOException {

    String line = reader.readLine();
    if (line == null) {
      return null;
    }
    while (!line.startsWith("E")) {
      line = reader.readLine();
      if (line == null) {
        return null;
      }
    }
    Matcher m = p3.matcher(line);
    m.find();

    // ignore malformed event line
    try {
    EventType type = EventType.getEnum(m.group(1));
    
    
    Entity trigger = new Entity("", "Trigger", parseTokens(m.group(2)));

    assert trigger != null : basename + ": " + m.group(2);

    Entity argument = new Entity("", "Protein", parseTokens(m.group(4)));
    assert argument != null : basename + ":" + line + "\n" + entities;
    
    ArgumentType argumentType = ArgumentType.getEnum(m.group(3));
    //System.out.println(argumentType);
    return new Event(basename, type, trigger, argumentType, argument, m.group(5));
    }
    catch(Exception e) {
    	return null;
    }
  }

  @Override
  public void close()
      throws IOException {
  }

  private static final Pattern p = Pattern
                                     .compile("([\\w$`',:-]+?)_([^_]+)_([\\d]+)_(\\d+)");

  public static List<Token> parseTokens(String str) {

    List<Token> tokens = new ArrayList<Token>();
    Matcher m = p.matcher(str);
    while (m.find()) {
      Token token = new Token(m.group(2), m.group(1), Integer.parseInt(m
          .group(3)), Integer.parseInt(m.group(4)));
      tokens.add(token);
    }
    
    // assert !tokens.isEmpty() : str;
    return tokens;
  }
}
