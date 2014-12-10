package mods.ptb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import mods.utils.BatchProcessor;
import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;

public class PtbAligment extends FileProcessor {

  Treebank         treebank;
  protected String text;

  public static void main(String[] args)
      throws IOException {
//    PtbAligment p = new PtbAligment();
//    if (args.length == 1) {
//      System.err.println("one file: " + args[0] + "!");
//      p.processFile(Env.DIR, args[0]);
//    } else {
//      System.err.println("no file!");
//    }
    PtbAligment fileProcessor = new PtbAligment();
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }

  @Override
  protected void readResource(String dir, String filename) {
    this.filename = filename;

    // read text
    text = Utils.readText(dir + "/" + filename + ".txt");

    PtbReader ptbReader = new PtbReader(Env.DIR_PARSE + filename + ".ptb");
    treebank = ptbReader.readTreebank();
  }

  @Override
  public final void processFile(String dir, String filename) {
    readResource(dir, filename);

    // alignment tree
    int textIndex = 0;
    for (Tree tree : treebank) {
      for (Tree tn : postorder(tree)) {
        if (tn.isLeaf()) {
          // parse failed
          if (tn.value().equals("Parse")) {
            break;
          }
          String value = Utils.adaptValue(tn.value());
          int from = text.indexOf(value, textIndex);
          int to = from + value.length();

          assert from != -1 : filename
              + ": "
                + textIndex
                + ": "
                + value
                + "\n"
                + text;
          textIndex = to;
          tn.setValue(tn.value() + "_" + from + "_" + to);
        }
      }
    }

    // output
    try  {PrintStream out = new PrintStream(new FileOutputStream( Env.DIR_SIMP + filename
        + ".ptb"));
      for (Tree tree : treebank) {
        out.println(tree);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private List<Tree> postorder(Tree root) {
    List<Tree> postorder = new LinkedList<Tree>();
    for (Tree child : root.children()) {
      if (child.isLeaf()) {
        postorder.add(child);
      } else {
        postorder.addAll(postorder(child));
      }
    }
    postorder.add(root);
    return postorder;
  }
}
