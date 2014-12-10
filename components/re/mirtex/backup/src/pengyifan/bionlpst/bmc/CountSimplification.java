package pengyifan.bionlpst.bmc;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.ptb.PtbReader;
import pengyifan.bionlpst.v3.simp.SimplificationPattern;
import pengyifan.bionlpst.v3.simp.SimplificationTregexReader;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class CountSimplification extends BatchProcessor {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) {
    CountSimplification p = new CountSimplification(
        new CountSimplificationFile());
    p.processDir(Env.DIR);
    System.out.println("Coordination\t" + p.cooSum);
    System.out.println("Relative clause\t" + p.relSum);
    System.out.println("Apposition\t" + p.appSum);
    System.out.println("Parenthesized elements\t" + p.parSum);
  }

  int cooSum, relSum, appSum, parSum;

  public CountSimplification(FileProcessor fileProcessor) {
    super(fileProcessor);
    cooSum = 0;
    relSum = 0;
    appSum = 0;
    parSum = 0;
  }

  @Override
  public void processDir(String dir) {
    for (File f : allFiles(dir)) {
      String filename = f.getName();
      filename = filename.substring(0, filename.lastIndexOf('.'));
      fileProcessor.processFile(dir, filename);
      CountSimplificationFile p = (CountSimplificationFile) fileProcessor;
      cooSum += p.cooSum;
      relSum += p.relSum;
      appSum += p.appSum;
      parSum += p.parSum;
    }
  }
}

class CountSimplificationFile extends FileProcessor {

  MemoryTreebank treebank;
  int            cooSum, relSum, appSum, parSum;

  public CountSimplificationFile() {
  }

  @Override
  protected void readResource(String dir, String filename) {
    PtbReader ptbReader = new PtbReader(dir + "/mccc/ptb2/" + filename + ".ptb");
    treebank = ptbReader.readTreebank();
  }

  @Override
  public final void processFile(String dir, String filename) {
    // System.out.println(filename);
    readResource(dir, filename);

    cooSum = 0;
    relSum = 0;
    appSum = 0;
    parSum = 0;

    for (Tree t : treebank) {
      count(t);
    }
  }

  public void count(Tree tree) {
    Set<Tree> set = new HashSet<Tree>();
    // coordination
    List<SimplificationPattern> list = SimplificationTregexReader
        .getTregex(SimplificationTregexReader.Coordination);
    for (SimplificationPattern p : list) {
      TregexMatcher m = p.getTregexPattern().matcher(tree);
      while (m.find()) {
        Tree t = m.getMatch();
        if (!set.contains(t)) {
          set.add(t);
          cooSum++;
        }
      }
    }
    // rel
    set.clear();
    list = SimplificationTregexReader
        .getTregex(SimplificationTregexReader.RelativeClause);
    for (SimplificationPattern p : list) {
      TregexMatcher m = p.getTregexPattern().matcher(tree);
      while (m.find()) {
        Tree t = m.getMatch();
        if (!set.contains(t)) {
          set.add(t);
          relSum++;
        }
      }
    }
    // app
    set.clear();
    list = SimplificationTregexReader
        .getTregex(SimplificationTregexReader.Apposition);
    for (SimplificationPattern p : list) {
      TregexMatcher m = p.getTregexPattern().matcher(tree);
      while (m.find()) {
        Tree t = m.getMatch();
        if (!set.contains(t)) {
          set.add(t);
          appSum++;
        }
      }
    }
    // par
    set.clear();
    list = SimplificationTregexReader
        .getTregex(SimplificationTregexReader.ParentThesis);
    for (SimplificationPattern p : list) {
      TregexMatcher m = p.getTregexPattern().matcher(tree);
      while (m.find()) {
        Tree t = m.getMatch();
        if (!set.contains(t)) {
          set.add(t);
          parSum++;
        }
      }
    }
  }

}
