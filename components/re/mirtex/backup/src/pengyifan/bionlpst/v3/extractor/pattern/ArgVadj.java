package pengyifan.bionlpst.v3.extractor.pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ArgVadj extends ExtractorPattern {

  public ArgVadj(String tregex) {
    super(tregex);
  }

  TregexPattern vadj;

  @Override
  public String getName() {
    return "Arg Vadj";
  }

  @Override
  protected void compile() {
    super.compile();
    vadj = TregexPattern.compile("VP <1 /VB.*/ <2 (ADJP=tr)");
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {
    boolean result = super.match(matcher, tregexMatcher, tree);
    if (result) {
      result = isVadj(matcher);
    }
    return result;
  }

  private boolean isVadj(ExtractorMatcher matcher) {
    TregexMatcher m = vadj.matcher(matcher.trigger);
    if (m.find() && matcher.trigger == m.getMatch()) {
      matcher.trigger = getLastJJ(m.getNode("tr"));
      return true;
    }

    Tree rightMostVp = RightMostVp.rightMostVp(matcher.trigger);
    for (Tree child : rightMostVp.children()) {
      m = vadj.matcher(child);
      if (m.find() && child == m.getMatch()) {
        matcher.trigger = getLastJJ(m.getNode("tr"));
        return true;
      }
    }
    return false;
  }

  private Tree getLastJJ(Tree tree) {
    // last JJ
    for (int i = tree.numChildren() - 1; i >= 0; i--) {
      Tree child = tree.getChild(i);
      if (child.value().equals("JJ")) {
        return child;
      }
    }
    return tree;
  }

}
