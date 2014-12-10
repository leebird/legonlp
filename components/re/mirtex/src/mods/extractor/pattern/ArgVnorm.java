package mods.extractor.pattern;

import java.util.List;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class ArgVnorm extends ExtractorPattern {

  public ArgVnorm(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Arg Vnorm";
  }
  
  @Override

  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher,
      Tree tree) {

    boolean result = super.match(matcher, tregexMatcher, tree);
    if (result) {
      List<Tree> children = matcher.getMatch().getChildrenAsList();
      int triggerIndex = children.indexOf(matcher.trigger);
      int argIndex = children.indexOf(matcher.argument);

      matcher.matched = tregexMatcher.getMatch();
      TreeFactory tf = tregexMatcher.getMatch().treeFactory();

      matcher.matched = tregexMatcher.getMatch().deepCopy(tf);
      children = matcher.matched.getChildrenAsList();
      
      matcher.trigger = tf.newTreeNode(
          "NP",
            children.subList(triggerIndex, children.size()));

      matcher.argument = tf
          .newTreeNode("NP", children.subList(0, argIndex + 1));

      while (matcher.matched.numChildren() != 0) {
        matcher.matched.removeChild(0);
      }
      matcher.matched.addChild(matcher.argument);
      matcher.matched.addChild(matcher.trigger);
    }
    return result;
  }
  
}

