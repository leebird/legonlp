package mods.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import mods.utils.Env;
import mods.simp.SimplificationPattern;
import mods.simp.SimplificationTregexReader;

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
