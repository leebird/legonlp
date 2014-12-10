package pengyifan.bionlpst.v2.ptb;

import java.io.File;
import java.util.Iterator;

import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.PennTreeReaderFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReaderFactory;
import edu.stanford.nlp.trees.Trees;

public class PtbReader {

  public final static TreeReaderFactory trf = new PennTreeReaderFactory(
                                                OffsetTreeFactory.instance());

  private final String                  filename;

  public PtbReader(String filename) {
    this.filename = filename;
  }

  public MemoryTreebank readTreebank() {
    MemoryTreebank treebank = new MemoryTreebank(trf);
    if (!new File(filename).isFile()) {
      return treebank;
    }
    treebank.loadPath(filename);
    // remove Parse failed
    Iterator<Tree> itr = treebank.iterator();
    while (itr.hasNext()) {
      Tree t = itr.next();
      Tree firstLeaf = Trees.getLeaf(t, 0);
      if (firstLeaf.value().equals("Parse")) {
        itr.remove();
      }
    }
    return treebank;
  }
}
