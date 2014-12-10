import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.CoEventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.ArgumentType;
import pengyifan.bionlpst.v2.annotation.Token;

public class DecomposeCoref {

  public static void main(String args[])
      throws IOException {
    FileProcessor fileProcessor = new DecomposeCorefFile();
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }
}

class DecomposeCorefFile extends FileProcessor {

  List<Event> events;

  @Override
  protected void readResource(String dir, String filename) {
    String cofilename = Env.DIR + "/bionlp-co/" + filename + ".a2";
    List<Entity> ts = A1EntityReader.readEntities(cofilename);
    events = CoEventReader.readEvents(cofilename, ts);
  }

  @Override
  public void processFile(String dir, String filename) {
    this.dir = dir;
    this.filename = filename;

    System.out.println(filename);

    readResource(dir, filename);

    // decompose
    List<Event> decompose = new ArrayList<Event>();
    for (Event e : events) {
      // if (e.filename.equals("PMID-7730624")) {
      // System.err.println();
      // }
      // trigger
      Token t = e.trigger.getFirst();
      int leftP = t.word.indexOf('(');
      if (leftP != -1 && leftP != 0 && t.word.charAt(leftP) == ' ') {
        Token first = new Token(t.word.substring(0, leftP - 1), "NN", t.from(),
            t.from() + leftP - 1);

        Token second = new Token(t.word.substring(
            leftP + 1,
            t.word.length() - 1), "NN", t.from() + leftP + 1, t.to() - 1);

        Entity newTrigger = getEntity(first);
        decompose.add(new Event(e.filename, e.type, newTrigger, e.argumentType,
            e.argument, e.comment));
        newTrigger = getEntity(second);
        decompose.add(new Event(e.filename, e.type, newTrigger, e.argumentType,
            e.argument, e.comment));
      }
      // argument
      t = e.argument.getFirst();
      if (leftP != -1 && leftP != 0 && t.word.charAt(leftP) == ' ') {
        Token first = new Token(t.word.substring(0, leftP - 1), "NN", t.from(),
            t.from() + leftP - 1);

        Token second = new Token(t.word.substring(
            leftP + 1,
            t.word.length() - 1), "NN", t.from() + leftP + 1, t.to() - 1);

        Entity newArgument = getEntity(first);
        decompose.add(new Event(e.filename, e.type, e.trigger,
            ArgumentType.Theme, newArgument, e.comment));
        newArgument = getEntity(second);
        decompose.add(new Event(e.filename, e.type, e.trigger,
            ArgumentType.Theme, newArgument, e.comment));
      }
      decompose.add(new Event(e.filename, e.type, getEntity(e.trigger
          .getFirst()), ArgumentType.Theme, getEntity(e.argument.getFirst()),
          e.comment));
    }

    try {
      filename = dir + "/bionlp-co/" + filename + ".a3";
      PrintStream out = new PrintStream(new FileOutputStream(filename));
      Collections.sort(events);
      for (Event e : decompose) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private Entity getEntity(Token t) {
    List<Token> tokens = new ArrayList<Token>();

    Pattern p = Pattern.compile("[^ ]+");
    Matcher m = p.matcher(t.word);
    while (m.find()) {
      Token x = new Token(m.group(), "NN", t.from() + m.start(), t.from()
          + m.end());
      tokens.add(x);
    }

    return new Entity("", "Protein", tokens);
  }
}
