import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.ptb.PtbReader;
import pengyifan.bionlpst.v2.ptb.PtbString;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;

public class PatternToTree extends FileProcessor {

  public static void main(String args[])
      throws IOException {

    String pattern = "[^ ]+ consisting of";
    String filename = "ref tree.txt";
    PrintStream out = new PrintStream(new FileOutputStream(filename));

    PatternToTree c = new PatternToTree(pattern, out);

    // args[0] = "PMC-1447668-19-Materials_and_Methods-09";
    if (args.length == 1) {
      c.processFile(Env.DIR, args[0]);
    } else {
      BatchProcessor b = new BatchProcessor(c);
      b.processDir(Env.DIR);
    }
  }

  public PatternToTree(String pattern, PrintStream out)
      throws FileNotFoundException {
    this.pattern = pattern;
    this.out = out;
  }

  Treebank    treebank;
  String      pattern;
  PrintStream out;

  @Override
  protected void readResource(String dir, String filename) {
    PtbReader ptbReader = new PtbReader(dir + "/mccc/ptb/" + filename + ".ptb");
    treebank = ptbReader.readTreebank();
  }

  @Override
  public void processFile(String dir, String filename) {
    System.out.println(filename);
    readResource(dir, filename);

    for (Tree t : treebank) {
      List<Tree> leaves = t.getLeaves();
      StringBuilder sb = new StringBuilder();
      for (Tree l : leaves) {
        sb.append(l.value() + " ");
      }
      Pattern p = Pattern.compile(pattern);
      Matcher m = p.matcher(sb.toString());
      if (m.find()) {
        int from = m.start();
        int to = m.end();
        int charindex = 0;

        Tree fromT = null;
        Tree toT = null;

        for (int i = 0; i < leaves.size(); i++) {
          Tree leaf = leaves.get(i);
          if (charindex == from) {
            fromT = leaf;
          }
          if (charindex + leaf.value().length() == to) {
            toT = leaf;
            break;
          }
          charindex += leaf.value().length() + 1;
        }

        if (fromT != null && toT != null) {
          // find parent
          Set<Tree> ps = new HashSet<Tree>();
          Tree parent = fromT.parent(t);
          while (parent != null) {
            ps.add(parent);
            parent = parent.parent(t);
          }
          parent = toT.parent(t);
          while (parent != null) {
            if (ps.contains(parent)) {
              break;
            }
            parent = parent.parent(t);
          }
          if (parent != null) {
            out.println(PtbString.pennString(parent));
            // System.exit(0);
          }
        }

      }

    }
  }
}
