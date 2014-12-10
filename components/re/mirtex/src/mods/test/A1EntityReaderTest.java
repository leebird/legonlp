package mods.test;

import java.io.FileReader;
import java.io.IOException;

import mods.utils.Env;
import mods.annotation.A1EntityReader;
import mods.annotation.Entity;

public class A1EntityReaderTest {

  public static void main(String[] args)
      throws IOException {
    String filename = Env.DIR + "/PMC-1447668-09-Results-07.a2";
    A1EntityReader reader = new A1EntityReader(new FileReader(filename));
    Entity e = null;
    while ((e = reader.readEntity()) != null) {
      System.out.println(e);
    }
    reader.close();
  }
}
