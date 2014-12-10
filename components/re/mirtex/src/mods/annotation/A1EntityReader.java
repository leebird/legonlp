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

public class A1EntityReader implements Closeable {

  public static List<Entity> readEntities(String filename) {
    File f = new File(filename);
    if (!f.exists()) {
      return Collections.emptyList();
    }
    List<Entity> ts = new ArrayList<Entity>();
    try {
      A1EntityReader reader = new A1EntityReader(new FileReader(filename));
      Entity e = null;
      while ((e = reader.readEntity()) != null) {
        ts.add(e);
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.err.println("no such file: " + filename);
    } catch (IOException e) {
      System.err.println("cannot read: " + filename);
    }
    return ts;
  }

  LineNumberReader       reader;

  private static Pattern p1 = Pattern
                                .compile("(T\\d+)\\t(\\w+) (\\d+) (\\d+)\\t([^\\t]+)");

  public A1EntityReader(FileReader reader) {
    this.reader = new LineNumberReader(reader);
  }

  public Entity readEntity()
      throws IOException {
    String line = reader.readLine();
    if (line == null) {
      return null;
    }
    while (!line.startsWith("T")) {
      line = reader.readLine();
      if (line == null) {
        return null;
      }
    }
    Matcher m = p1.matcher(line);
    m.find();
    Token t = new Token(m.group(5), "NN", Integer.parseInt(m.group(3)),
        Integer.parseInt(m.group(4)));
    return new Entity(m.group(1), m.group(2), Collections.singletonList(t));
  }

  @Override
  public void close()
      throws IOException {
    reader.close();
  }
}
