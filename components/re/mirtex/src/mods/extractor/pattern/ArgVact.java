package mods.extractor.pattern;

import java.util.regex.Pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class ArgVact extends ExtractorPattern {

  public ArgVact(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Arg Vact";
  }

  Pattern vact;

  @Override
  protected void compile() {
    super.compile();
    vact = Pattern.compile("^VBD|VBZ|VBP|VBG|VB|VBN$");
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {
    boolean result = super.match(matcher, tregexMatcher, tree);
    if (result) {
      result = isVact(matcher);
    }
    return result;
  }

  private boolean isVact(ExtractorMatcher matcher) {
    Tree rightMostVp = RightMostVp.rightMostVp(matcher.trigger);
    for (Tree child : rightMostVp.children()) {
      if (vact.matcher(child.value()).find()) {
        matcher.trigger = child;
        return true;
      }
    }
    return false;
  }
}
