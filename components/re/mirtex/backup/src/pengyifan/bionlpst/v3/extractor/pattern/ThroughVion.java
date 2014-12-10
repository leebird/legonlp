package pengyifan.bionlpst.v3.extractor.pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ThroughVion extends ExtractorPattern {

  public ThroughVion(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Arg does something through -ion";
  }

  TregexPattern through1;
  TregexPattern through2;

  @Override
  protected void compile() {
    super.compile();
    through1 = TregexPattern
        .compile("VP <1 /VB.*/ <-1 (PP <<, /through/ <2 (NP <1 (NP=tr << /ion/) ! << /of/  ! << /PRP/ ))");
    through2 = TregexPattern
        .compile("VP <1 /VB.*/ <-1 (PP <<, /through/ <2 (NP=tr << /ion/) ! << /of/ ! << /PRP/ )");
  }

  @Override
  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {
    boolean result = super.match(matcher, tregexMatcher, tree);
    if (result) {
      result = through(matcher);
    }
    return result;
  }

  private boolean through(ExtractorMatcher matcher) {
    Tree rightMostVp = RightMostVp.rightMostVp(matcher.trigger);
    TregexMatcher m = through1.matcher(rightMostVp);
    if (m.find()) {
      matcher.trigger = m.getNode("tr");
      return true;
    }
    m = through2.matcher(rightMostVp);
    if (m.find()) {
      matcher.trigger = m.getNode("tr");
      return true;
    }
    return false;
  }
}
