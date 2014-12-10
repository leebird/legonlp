package mods.simp;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.ptb.PtbReader;
import mods.ptb.PtbUtils;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class GenerateParCooSimplification extends FileProcessor {

  boolean        isSimplified;
  int            index;
  MemoryTreebank treebank;

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) {
    GenerateParCooSimplification p = new GenerateParCooSimplification();

    if (args.length == 0) {
      args = new String[] { "PMC-3062687-12-Methods" };
      p.debug = true;
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  public GenerateParCooSimplification() {
    index = -1;
  }

  @Override
  protected void readResource(String dir, String filename) {
    // read ptb
    PtbReader ptbReader = new PtbReader(Env.DIR_SIMP + filename + ".ptb");
    treebank = ptbReader.readTreebank();
    // prune "in vivo"
    for (Tree t : treebank) {
      PtbUtils.prune(t);
    }
  }

  @Override
  public final void processFile(String dir, String filename) {

    if (filename.equals("PMC-3062687-12-Methods")) {
      return;
    }
    super.processFile(dir, filename);

    if (index != -1) {
      MemoryTreebank newTreebank = new MemoryTreebank();
      newTreebank.add(treebank.get(index));
      treebank = newTreebank;
    }

    // general
    Treebank simpTreebank = new MemoryTreebank();
    try {
      for (Tree t : treebank) {
        simpTreebank.addAll(simplify(t));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // output
    try 
         {
      PrintStream out = new PrintStream(new FileOutputStream(Env.DIR_SIMP
          + filename
          + ".ptb.simp.parcoo"));
      Set<String> noDuplicates = new HashSet<String>();
      for (Tree tree : simpTreebank) {
        String line = tree.toString();
        if (!noDuplicates.contains(line)) {
          out.println(line);
          noDuplicates.add(line);
        }
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static final TregexPattern cooPattern = TregexPattern
                                            .compile("__ < CC|CONJP");
  Set<String>                simps      = new HashSet<String>();

  public Treebank simplify(Tree tree) {

    Treebank totalSimplified = new MemoryTreebank();

    Queue<Tree> queue = new LinkedList<Tree>();
    queue.offer(tree);
    while (!queue.isEmpty()) {
      Tree t = queue.poll();

      boolean hasSimplification = false;

      int types[] = new int[] { //
      Simplifier.Parenthesis, //
          Simplifier.Coordination };

      for (int type : types) {
        hasSimplification = simplify(t, queue, totalSimplified, type);
        if (hasSimplification) {
          break;
        }
      }

      if (!hasSimplification) {
        if (t != tree) {
          totalSimplified.add(t);
        }
      }
    }
    return totalSimplified;
  }

  private boolean simplify(Tree t, Queue<Tree> queue, Treebank totalSimplified,
      int type) {
    Simplifier simplifier = Simplifier.matcher(t, type);
    if (simplifier.find()) {
      for (Tree simplifiedTree : simplifier.getSimplifiedTrees()) {
        if (!simps.contains(simplifiedTree.toString())) {
          simps.add(simplifiedTree.toString());
          queue.offer(simplifiedTree);
        }
      }
      return true;
    }
    return false;
  }
}
