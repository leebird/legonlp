package pengyifan.bionlpst.v2.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.annotation.Event.EventType;

public class Merge2A4 extends FileProcessor {

  @Override
  public void processFile(String dir, String filename) {
    super.processFile(dir, filename);
    // generate a4
    printA4(dir, filename);
  }

  protected void printA4(String dir, String filename) {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/"
          + filename
          + ".a4"));
      for (EventType type : EventType.values()) {
        File a3file = new File(dir + "/" + filename + ".a3." + type);
        if (!a3file.exists()) {
          continue;
        }
        LineNumberReader r = new LineNumberReader(new FileReader(a3file));
        String line = null;
        while ((line = r.readLine()) != null) {
          out.println(line);
        }
        r.close();
      }
      out.close();
      // activity
      // File a3file = new File(dir + "/" + filename + ".a3.activity");
      // LineNumberReader r = new LineNumberReader(new FileReader(a3file));
      // String line = null;
      // while ((line = r.readLine()) != null) {
      // out.println(line);
      // }
      // r.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Merge2A4 p = new Merge2A4();
    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  @Override
  protected void readResource(String dir, String filename) {
  }

}
