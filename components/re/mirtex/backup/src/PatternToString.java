import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.A2EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;

public class PatternToString extends FileProcessor {

  public static void main(String args[])
      throws IOException {

    String pattern = "^binding domain";
    String filename = "activity.txt";
    PrintStream out = new PrintStream(new FileOutputStream(filename));

    PatternToString c = new PatternToString(pattern, out);

    // args[0] = "PMC-1447668-19-Materials_and_Methods-09";
    if (args.length == 1) {
      c.processFile(Env.DIR, args[0]);
    } else {
      BatchProcessor b = new BatchProcessor(c);
      b.processDir(Env.DIR);
    }
  }

  public PatternToString(String pattern, PrintStream out)
      throws FileNotFoundException {
    this.pattern = pattern;
    this.out = out;
  }

  String      text;
  String      pattern;
  List<Event> a2es;
  PrintStream out;

  @Override
  protected void readResource(String dir, String filename) {
    text = Utils.readText(dir + "/" + filename + ".txt");
    
    String a1filename = dir + "/" + filename + ".a1";
    String a2filename = dir + "/" + filename + ".a2";
    
    List<Entity> entities = A1EntityReader.readEntities(a1filename);
    entities.addAll(A1EntityReader.readEntities(a2filename));
    a2es = A2EventReader.readEvents(a1filename, a2filename, entities);
  }

  @Override
  public void processFile(String dir, String filename) {
    System.out.println(filename);
    readResource(dir, filename);
    for (Event e : a2es) {
      String estr = text.substring(e.trigger.from());
      Matcher m = Pattern.compile(pattern).matcher(estr);
      
      int from = Math.min(e.trigger.from(), e.argument.from());
      int to = Math.max(e.trigger.to(), e.argument.to());
      if (m.find()) {
        out.printf("%s\n\t%s\n\n", text.substring(from, to), e);
      }
    }
  }
}
