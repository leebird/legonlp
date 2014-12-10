package pengyifan.bionlpst.v3.test;

import java.io.IOException;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.annotation.A3EventReader;
import pengyifan.bionlpst.v2.annotation.Event;

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
