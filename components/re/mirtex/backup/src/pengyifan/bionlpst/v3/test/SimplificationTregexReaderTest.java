package pengyifan.bionlpst.v3.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v3.simp.SimplificationPattern;
import pengyifan.bionlpst.v3.simp.SimplificationTregexReader;

public class SimplificationTregexReaderTest {

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    SimplificationTregexReader reader = new SimplificationTregexReader(
        new FileReader(Env.COORDINATION_TREGEX));
    List<SimplificationPattern> list = reader.readTregex();
    reader.close();
    
    for(SimplificationPattern p: list) {
      System.out.println(p);
    }
  }
}
