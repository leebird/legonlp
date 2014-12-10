package pengyifan.bionlpst.v3.simp;

import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.ptb.TOperationPattern;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonParseException;

public class SimplificationTregexReader implements Closeable {

  public static final int                          Coordination      = 1;
  public static final int                          RelativeClause    = 2;
  public static final int                          ParentThesis      = 3;
  public static final int                          Apposition        = 4;
  public static final int                          SentenceBeginning = 6;
  public static final int                          Others            = 8;

  private static final List<SimplificationPattern> list              = new ArrayList<SimplificationPattern>();
  private static final List<SimplificationPattern> coolist           = new ArrayList<SimplificationPattern>();
  private static final List<SimplificationPattern> rellist           = new ArrayList<SimplificationPattern>();
  private static final List<SimplificationPattern> parlist           = new ArrayList<SimplificationPattern>();
  private static final List<SimplificationPattern> applist           = new ArrayList<SimplificationPattern>();
  private static final List<SimplificationPattern> senbeglist        = new ArrayList<SimplificationPattern>();
  private static final List<SimplificationPattern> otherslist        = new ArrayList<SimplificationPattern>();

  public static List<SimplificationPattern> getTregex() {
    if (list.isEmpty()) {
      read();
    }
    return list;
  }

  public static List<SimplificationPattern> getTregex(int type) {
    if (list.isEmpty()) {
      read();
    }
    switch (type) {
    case Coordination:
      return coolist;
    case RelativeClause:
      return rellist;
    case Apposition:
      return applist;
    case ParentThesis:
      return parlist;
    case SentenceBeginning:
      return senbeglist;
    case Others:
      return otherslist;
    default:
      return list;
    }
  }

  private static void read() {
    list.clear();
    read(Env.PARENTHESIS_TREGEX, parlist);
    read(Env.COORDINATION_TREGEX, coolist);
    read(Env.RELATIVE_TREGEX, rellist);
    read(Env.APPOSITION_TREGEX, applist);
    read(Env.SENBEG_TREGEX, senbeglist);
    read(Env.OTHERS_TREGEX, otherslist);
    // list.addAll(nppplist);
    list.addAll(parlist);
    list.addAll(applist);
    list.addAll(coolist);
    list.addAll(rellist);
    list.addAll(senbeglist);
    list.addAll(otherslist);
  }

  private static void read(String filename, List<SimplificationPattern> list) {
    try {
      SimplificationTregexReader reader = new SimplificationTregexReader(
          new FileReader(filename));
      list.addAll(reader.readTregex());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  LineNumberReader reader;

  public SimplificationTregexReader(FileReader reader) {
    this.reader = new LineNumberReader(reader);
  }

  public List<SimplificationPattern> readTregex()
      throws IOException {
    List<SimplificationPattern> list = new LinkedList<SimplificationPattern>();
    List<TregexPattern> patterns = new ArrayList<TregexPattern>();
    List<TOperationPattern> listOfOpts = new ArrayList<TOperationPattern>();

    String line = null;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("#") || line.startsWith("//")) {
        continue;
      } else if (line.isEmpty()) {
        if (!patterns.isEmpty()) {
          for (TregexPattern p : patterns) {
            list.add(new SimplificationPattern(p, listOfOpts));
          }
          patterns.clear();
          listOfOpts = new ArrayList<TOperationPattern>();
        }
        continue;
      }
      int index = line.indexOf(':');
      if (index == -1) {
        throw new IllegalArgumentException("cannot parse line["
            + reader.getLineNumber()
            + "]: "
            + line);
      } else {
        String name = line.substring(0, index).trim();
        String value = line.substring(index + 1).trim();
        if (name.equals("operation")) {
          try {
            listOfOpts.add(TOperationPattern.parseOperation(value));
          } catch (TsurgeonParseException exception) {
            throw new IllegalArgumentException("cannot parse line["
                + reader.getLineNumber()
                + "]: "
                + line);
          }
        } else if (name.equals("tregex")) {
          patterns.add(TregexPattern.compile(value));
        } else {
          throw new IllegalArgumentException("cannot parse line["
              + reader.getLineNumber()
              + "]: "
              + line);
        }
      }
    }
    if (!patterns.isEmpty()) {
      for (TregexPattern p : patterns) {
        list.add(new SimplificationPattern(p, listOfOpts));
      }
    }
    return list;
  }

  @Override
  public void close()
      throws IOException {
    reader.close();
  }
}
