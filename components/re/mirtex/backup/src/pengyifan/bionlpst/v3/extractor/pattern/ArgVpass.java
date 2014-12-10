package pengyifan.bionlpst.v3.extractor.pattern;

import java.util.regex.Pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class ArgVpass extends ExtractorPattern {

  public ArgVpass(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Arg Vpass";
  }

  Pattern vpass;

  @Override
  protected void compile() {
    super.compile();
    vpass = Pattern.compile("^VBN$");
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {
    boolean result = super.match(matcher, tregexMatcher, tree);
    if (result) {
      result = isVpass(matcher);
    }
    return result;
  }

  private boolean isVpass(ExtractorMatcher matcher) {
    Tree rightMostVp = RightMostVp.rightMostVp(matcher.trigger);
    for (Tree child : rightMostVp.children()) {
      if (vpass.matcher(child.value()).find()) {
        matcher.trigger = child;
        return true;
      }
    }
    return false;
  }

}
