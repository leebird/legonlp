package pengyifan.bionlpst.v2.ptb;

import java.util.List;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;

public class OffsetTreeFactory implements TreeFactory {

  private static final OffsetLabelFactory lf       = OffsetLabelFactory.instance();

  private static OffsetTreeFactory        instance = null;

  private OffsetTreeFactory() {

  }

  public static OffsetTreeFactory instance() {
    if (instance == null) {
      instance = new OffsetTreeFactory();
    }
    return instance;
  }

  @Override
  public Tree newLeaf(String word) {
    int lastUnderline = word.lastIndexOf('_');
    if (lastUnderline == -1) {
      return new OffsetTree(lf.newLabel(word));
    }

    try {
      int to = Integer.parseInt(word.substring(lastUnderline + 1));

      int secondLastUnderline = word.lastIndexOf('_', lastUnderline - 1);

      if (secondLastUnderline == -1) {
        return new OffsetTree(lf.newLabel(word));
      }

      int from = Integer.parseInt(word.substring(
          secondLastUnderline + 1,
            lastUnderline));

      return new OffsetTree(lf.newLabel(
          word.substring(0, secondLastUnderline),
            from,
            to));
    } catch (NumberFormatException e) {
      System.err.println("cannot parse: " + e.getMessage());
      return new OffsetTree(lf.newLabel(word));
    }
  }

  @Override
  public Tree newLeaf(Label label) {
    return new OffsetTree(label);
  }

  @Override
  public Tree newTreeNode(String parent, List<Tree> children) {
    return new OffsetTree(lf.newLabel(parent), children);
  }

  @Override
  public Tree newTreeNode(Label parent, List<Tree> children) {
    return newTreeNode(parent.value(), children);
  }

}
