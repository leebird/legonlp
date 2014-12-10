import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.A2EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Token;

public class ChangeGoldFile extends FileProcessor {

  public static void main(String args[]) {
    FileProcessor fileProcessor = new ChangeGoldFile();
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }

  protected String      text;
  protected List<Event> goldes;

  @Override
  protected void readResource(String dir, String filename) {
    String a1filename = dir + "/" + filename + ".a1";
    String a2filename = dir + "/" + filename + ".a2";

    // read text
    text = Utils.readText(dir + "/" + filename + ".txt");

    // read gold
    List<Entity> a1ts = A1EntityReader.readEntities(a1filename);
    List<Entity> a2ts = A1EntityReader.readEntities(a2filename);
    List<Entity> a1a2ts = new LinkedList<Entity>();
    a1a2ts.addAll(a1ts);
    a1a2ts.addAll(a2ts);
    goldes = A2EventReader.readEvents(a1filename, a2filename, a1a2ts);
  }

  @Override
  public final void processFile(String dir, String filename) {

    System.out.println(filename);
    super.processFile(dir, filename);

    for (int i = 0; i < goldes.size(); i++) {

      Event goldE = goldes.get(i);
      int tf = goldE.trigger.from();
      int tt = goldE.trigger.to();

      int newtf = extendLeft(text, tf, tt);
      if (newtf != -1) {
        tf = newtf;
      }

      int newtt = extendRight(text, tf, tt);
      if (newtt != -1) {
        tt = newtt;
      }

      newtf = shrinkLeft(text, tf, tt);
      if (newtf != -1) {
        tf = newtf;
      }

      if (tt != goldE.trigger.to() || tf != goldE.trigger.from()) {
        Token t = new Token(text.substring(tf, tt), "NN", tf, tt);
        Entity newTrigger = new Entity(goldE.trigger.id, goldE.trigger.type,
            Collections.singletonList(t));
        goldes.set(i, new Event(goldE.filename, goldE.type, newTrigger, goldE.argumentType,
            goldE.argument, goldE.comment));
      }
    }

    // a3
    try {
      filename = dir + "/" + filename + ".gld";
      PrintStream out = new PrintStream(new FileOutputStream(filename));
      Collections.sort(goldes);
      for (Event e : goldes) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * @param text
   * @param from
   * @return new to
   */
  private int extendRight(String text, int from, int to) {
    // binding (activity) --> binding activity
    String triggers[] = new String[] { //
    "activity", //
        "activities", //
        "analysis", //
        "subunits",//
        "subunit",//
        "complexes",//
        "complex",//
        "characteristic",//
        "property",//
        "properties",//
        "sites",//
        "site",//
        "elements",//
        "element",//
        "states",//
        "state",//
        "sequences",//
        "sequence",//
        "proteins",//
        "protein",//
        "regions",//
        "region",//
        "domains",//
        "domain",//
        "patterns",//
        "pattern",//
        "genotypes",//
        "genotype",//
        "study", //
        "studies",//
    };

    String test = text.substring(from, to);
    if (test.equals("binding")
        || test.equalsIgnoreCase("dna-binding")
        || test.equalsIgnoreCase("transcription")) {
      for (String t : triggers) {
        int newto = to + t.length() + 1;
        if (newto < text.length()) {
          test = text.substring(to + 1, newto);
          if (test.equalsIgnoreCase(t)) {
            return newto;
          }
        }
      }
    }

    // phosphorylation (status) --> phosphorylation status
    triggers = new String[] { //
    "status", //
        "pattern", //
        "form" };
    if (test.equals("phosphorylation") || test.endsWith("phosphorylated")) {
      for (String t : triggers) {
        int newto = to + t.length() + 1;
        if (newto < text.length()) {
          test = text.substring(to + 1, newto);
          if (test.equalsIgnoreCase(t)) {
            return newto;
          }
        }
      }
    }

    return -1;
  }

  private int extendLeft(String text, int from, int to) {
    String triggers[] = { "binding", "bound" };
    for (String t : triggers) {
      String test = text.substring(from, to);
      if (test.equalsIgnoreCase(t)) {
        // dna-binding
        if (from - 4 >= 0) {
          String dna = text.substring(from - 4, from);
          if (dna.equalsIgnoreCase("dna-")) {
            return from - 4;
          }
        }
      }
    }
    return -1;
  }

  /**
   * @param text
   * @param from
   * @return new from
   */
  private int shrinkLeft(String text, int from, int to) {
    // complex binding --> binding
    String triggers[] = { "complex binding", "gene transcription",
        "mrna expression", "gene expression", "cytokine production",
        "primary source", "gene transfer", "hybridization signals",
        "comparable levels", "spontaneous expression", "protein levels",
        "mrna transcripts", "not transcribed",
        "expression at the transcriptional level", "proteolytic degradation",
        "proteolytically degraded", "complete degradation",
        "protein secretion", "physical interaction", "physical interactions",
        "heterodimeric binding complex", "complex formed",
        "physical association", "capable of forming functional heterodimers",
        "its specific receptor", "physically interact", "complex binding" };
    for (String t : triggers) {
      String test = text.substring(from, to);
      if (test.equalsIgnoreCase(t)) {
        return from + t.lastIndexOf(' ') + 1;
      }
    }

    // [mRNA] level --> mRNA level
    // String triggers[] = { "complex binding" };
    // for (String t : triggers) {
    // int to = from + t.length();
    // if (to < text.length()) {
    // String test = text.substring(from, to);
    // if (test.equalsIgnoreCase(t)) {
    // return from + t.indexOf(' ') + 1;
    // }
    // }
    // }
    return -1;
  }

}