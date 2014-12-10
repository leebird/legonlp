package pengyifan.bionlpst.v3.extractor.pattern;

import java.util.List;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class ArgLocus extends ExtractorPattern {

  public ArgLocus(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Arg Vnorm";
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {

    boolean result = super.match(matcher, tregexMatcher, tree);
    if (result) {
      List<Tree> children = matcher.getMatch().getChildrenAsList();

      int argIndex = children.indexOf(matcher.argument);

      matcher.matched = tregexMatcher.getMatch().deepCopy();
      children = matcher.matched.getChildrenAsList();

      matcher.trigger = matcher.matched;

      TreeFactory tf = matcher.matched.treeFactory();
      matcher.argument = tf
          .newTreeNode("NP", children.subList(0, argIndex + 1));
    }
    return result;
  }
}
