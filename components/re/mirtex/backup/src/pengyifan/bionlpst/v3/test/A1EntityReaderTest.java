package pengyifan.bionlpst.v3.test;

import java.io.FileReader;
import java.io.IOException;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.Entity;

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
