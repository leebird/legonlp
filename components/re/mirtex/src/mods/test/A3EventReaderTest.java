package mods.test;

import java.io.IOException;

import mods.utils.Env;
import mods.annotation.A3EventReader;
import mods.annotation.Event;

public class A3EventReaderTest {

  public static void main(String[] args)
      throws IOException {

    A3EventReader reader = new A3EventReader(Env.DIR
        + "/PMC-1447668-09-Results-07.a3");
    Event e = null;
    while ((e = reader.readEvent()) != null) {
      System.out.println(e);
    }
    reader.close();
  }
}
