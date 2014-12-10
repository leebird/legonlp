package pengyifan.bionlpst.bmc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;
import pengyifan.bionlpst.v2.eval.EvalFile;
import pengyifan.bionlpst.v2.eval.EvalFilteredFile;

public class EvalPrinter extends BatchProcessor {

  List<Event>                  fp;
  List<Event>                  tp;
  List<Event>                  fn;
  protected String             text;
  protected List<Event>        predes;
  protected List<Event>        goldes;
  protected static PrintStream display = System.err;

  public static void main(String[] args)
      throws Exception {
    EvalFile ef = new EvalFilteredFile();
    EvalPrinter eval = new EvalPrinter(ef);
    eval.processDir(Env.DIR);
    eval.print();
    
    // file
    String fileprefix = Env.basedir + "/" + Env.DATA_SET;
    print(fileprefix + ".tp", eval.tp);
    print(fileprefix + ".fp", eval.fp);
    print(fileprefix + ".fn", eval.fn);
  }

  private static void print(String filename, List<Event> events) throws FileNotFoundException {
    PrintStream out = new PrintStream(new FileOutputStream(filename));
    for (Event e : events) {
      out.println(e.filename + "\t" + e);
    }
    out.close();
  }

  public EvalPrinter(EvalFile ef) {
    super(ef);
    fp = new ArrayList<Event>();
    tp = new ArrayList<Event>();
    fn = new ArrayList<Event>();
  }

  @Override
  public void processDir(String dir) {
    for (File f : allFiles(dir)) {
      String filename = f.getName();
      filename = filename.substring(0, filename.lastIndexOf('.'));
      EvalFile evalFile = (EvalFile) fileProcessor;
      evalFile.processFile(dir, filename);
      tp.addAll(evalFile.tp);
      fp.addAll(evalFile.fp);
      fn.addAll(evalFile.fn);
    }

  }

  public void print() {
    print("Whole", tp, fp, fn);
    List<Event> tpList, fnList, fpList;

    tpList = subList(tp, "PMID");
    fpList = subList(fp, "PMID");
    fnList = subList(fn, "PMID");
    display.println();
    print("Abstract", tpList, fpList, fnList);

    tpList = subList(tp, "PMC");
    fpList = subList(fp, "PMC");
    fnList = subList(fn, "PMC");
    display.println();
    print("Full", tpList, fpList, fnList);
    
    try {
      print("dev-trans-all.fn", fn);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private
      void
      print(String name, List<Event> tp, List<Event> fp, List<Event> fn) {
    int tpSum, fpSum, fnSum, tpn, fpn, fnn;
    List<Event> tpList, fnList, fpList;

    EventType types[] = new EventType[] { EventType.Gene_expression,
        EventType.Transcription, EventType.Protein_catabolism,
        EventType.Phosphorylation, EventType.Localization };

    display.println("-- " + name + "--");
    printTitle();
    tpSum = 0;
    fpSum = 0;
    fnSum = 0;

    for (EventType type : types) {
      tpList = subList(tp, type);
      fpList = subList(fp, type);
      fnList = subList(fn, type);
      tpn = tpList.size();
      fpn = fpList.size();
      fnn = fnList.size();
      printRow(type.toString(), tpn, fpn, fnn);
      tpSum += tpn;
      fpSum += fpn;
      fnSum += fnn;
    }
    // Simple Event
    printRow("   Simple Event", tpSum, fpSum, fnSum);
    // binding
    EventType type = EventType.Binding;
    tpList = subList(tp, type);
    fpList = subList(fp, type);
    fnList = subList(fn, type);
    tpn = tpList.size();
    fpn = fpList.size();
    fnn = fnList.size();
    printRow("   " + type.toString(), tpn, fpn, fnn);
    // total
    tpSum += tpn;
    fpSum += fpn;
    fnSum += fnn;
    printRow("   Total", tpSum, fpSum, fnSum);
  }

  protected List<Event> subList(List<Event> list, EventType type) {
    List<Event> subList = new ArrayList<Event>();
    for (Event e : list) {
      if (e.type == type) {
        subList.add(e);
      }
    }
    return subList;
  }

  protected List<Event> subList(List<Event> list, String filenamePrefix) {
    List<Event> subList = new ArrayList<Event>();
    for (Event e : list) {
      if (e.filename.startsWith(filenamePrefix)) {
        subList.add(e);
      }
    }
    return subList;
  }

  protected void printTitle() {
    String title = String.format(
        "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
        "type",
        "tp",
        "fp",
        "fn",
        "prec",
        "recall",
        "f1",
        "P/R/F");
    // title
    display.println(title);
    title = new String(new char[title.length()]).replace("\0", "-");
    display.println(title);
  }

  protected void printRow(String type, int tp, int fp, int fn) {
    double prec = ((double) tp) / (tp + fp);
    double recall = ((double) tp) / (tp + fn);
    double f1 = 2 * prec * recall / (prec + recall);
    // String row = String.format(
    // "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
    // type,
    // Integer.toString(tp),
    // Integer.toString(fp),
    // Integer.toString(fn),
    // String.format("%.4f", prec),
    // String.format("%.4f", recall),
    // String.format("%.4f", f1),
    // String.format("%2.2f/%2.2f/%2.2f", prec * 100, recall * 100, f1 * 100));
    String row = String.format(
        "%s",
        String.format("%2.2f/%2.2f/%2.2f", prec * 100, recall * 100, f1 * 100));
    display.println(row);
  }
}
