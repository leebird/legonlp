package pengyifan.bionlpst.v3.extractor.pattern;

import java.util.Collections;

import pengyifan.bionlpst.v2.ptb.OffsetLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class ArgDashExpressing extends ExtractorPattern {

  public ArgDashExpressing(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Arg - expressing";
  }

  @Override
  protected void compile() {
    super.compile();
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {

    Tree leaf = tregexMatcher.getMatch().getChild(0);
    if (!leaf.isLeaf()) {
      return false;
    }

    OffsetLabel label = (OffsetLabel) leaf.label();
    int dash = label.value().lastIndexOf('-');
    if (dash == -1 || dash == 0) {
      return false;
    }

    matcher.matched = leaf.parent(tree).deepCopy();

    TreeFactory tf = matcher.matched.treeFactory();

    OffsetLabel newLeafLabel = new OffsetLabel(label.value()
        .substring(dash + 1));
    newLeafLabel.setBeginPosition(dash + 1 + label.beginPosition());
    newLeafLabel.setEndPosition(label.endPosition());
    
    leaf = tf.newLeaf(newLeafLabel);
    matcher.trigger = tf.newTreeNode("NP", Collections.singletonList(leaf));
    
    newLeafLabel = new OffsetLabel(label.value()
        .substring(0, dash));
    newLeafLabel.setBeginPosition(label.beginPosition());
    newLeafLabel.setEndPosition(label.beginPosition() + dash);
    
    leaf = tf.newLeaf(newLeafLabel);
    matcher.argument = tf.newTreeNode("NP", Collections.singletonList(leaf));

    matcher.matched.removeChild(0);
    matcher.matched.addChild(matcher.argument);
    matcher.matched.addChild(matcher.trigger);
    return true;
  }
}
