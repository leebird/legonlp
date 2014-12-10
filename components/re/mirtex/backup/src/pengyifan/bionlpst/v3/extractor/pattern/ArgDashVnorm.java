package pengyifan.bionlpst.v3.extractor.pattern;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pengyifan.bionlpst.v2.ptb.OffsetLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class ArgDashVnorm extends ExtractorPattern {

  public  ArgDashVnorm(String tregex) {
    super(tregex);
  }

  Pattern p;

  @Override
  public String getName() {
    return "Arg - Vnorm";
  }

  Pattern vact;

  @Override
  protected void compile() {
    super.compile();
    p = Pattern.compile("(.+?[-].+?)[-]([^_\\d]+)");
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {

    Tree leaf = tregexMatcher.getMatch().getChild(0);
    if (!leaf.isLeaf()) {
      return false;
    }

    OffsetLabel label = (OffsetLabel) leaf.label();
    if (label.value().contains("-RRB-")) {
      return false;
    }
    Matcher m = p.matcher(label.value());
    if (!m.find()) {
      return false;
    }

    matcher.matched = leaf.parent(tree).deepCopy();
    
    TreeFactory tf = matcher.matched.treeFactory();

    OffsetLabel newLeafLabel = new OffsetLabel(m.group(2));
    newLeafLabel.setBeginPosition(m.start(2) + label.beginPosition());
    newLeafLabel.setEndPosition(m.end(2) + label.beginPosition());
    
    leaf = tf.newLeaf(newLeafLabel);
    matcher.trigger = tf.newTreeNode(matcher.matched.value(), Collections.singletonList(leaf));
    
    newLeafLabel = new OffsetLabel(m.group(1));
    newLeafLabel.setBeginPosition(m.start(1) + label.beginPosition());
    newLeafLabel.setEndPosition(m.end(1) + label.beginPosition());
    
    leaf = tf.newLeaf(newLeafLabel);
    matcher.argument = tf.newTreeNode(matcher.matched.value(), Collections.singletonList(leaf));

    matcher.matched.removeChild(0);
    matcher.matched.addChild(matcher.argument);
    matcher.matched.addChild(matcher.trigger);
    return true;
  }
}
