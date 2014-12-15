package tregex.ptb;

import java.util.List;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;

@SuppressWarnings("serial")
public class OffsetTree extends LabeledScoredTreeNode {

  public OffsetTree(Label label) {
    super(label);
  }

  public OffsetTree(Label label, List<Tree> children) {
    super(label);
    setChildren(children);
  }

  @Override
  public TreeFactory treeFactory() {
    return OffsetTreeFactory.instance();
  }

  @Override
  public Tree deepCopy() {
    return super.deepCopy(OffsetTreeFactory.instance());
  }

  @Override
  public LabelFactory labelFactory() {
    return OffsetLabelFactory.instance();
  }

  @Override
  public String toString() {
    return toStringBuilder(new StringBuilder(), false).toString();
  }
}