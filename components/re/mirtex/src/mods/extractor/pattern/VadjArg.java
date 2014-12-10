package mods.extractor.pattern;

import java.util.List;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class VadjArg extends ExtractorPattern {

  public VadjArg(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Vadj Arg";
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {

    boolean result = super.match(matcher, tregexMatcher, tree);
    if (result) {
      List<Tree> children = matcher.getMatch().getChildrenAsList();
      int triggerIndex = children.indexOf(matcher.trigger);
      int argIndex = children.indexOf(matcher.argument);

      matcher.matched = tregexMatcher.getMatch().deepCopy();
      children = matcher.matched.getChildrenAsList();

      matcher.trigger = children.get(triggerIndex);

      TreeFactory tf = matcher.matched.treeFactory();
      matcher.argument = tf.newTreeNode(
          "NP",
          children.subList(argIndex, children.size()));
    }
    return result;
  }
}
