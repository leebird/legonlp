package mods.extractor.pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ByVbg extends ExtractorPattern {

  public ByVbg(String tregex) {
    super(tregex);
  }

  @Override
  public String getName() {
    return "Arg does something by Vvbg";
  }

  TregexPattern by1;
  TregexPattern by2;
  TregexPattern by3;
  TregexPattern by4;
  TregexPattern by5;
  TregexPattern by6;
  
  @Override
  protected void compile() {
    super.compile();
    by1 = TregexPattern
        .compile("VP <-1 (PP <<, /by|via|through/ <2 (S <1 (VP <1 VBG=tr)))");
    
    by2 = TregexPattern
        .compile("VP <-1 (PP <<, /by|via|through/ <2 (S <1 ADVP <2 (VP <1 VBG=tr)))");
    
    by3 = TregexPattern
            .compile("VP <-1 (PP <<, /by|via|through/ <2 (S <1 (VP <1 ADVP <2 VBG=tr)))");
    
    by4 = TregexPattern
        .compile("VP <-1 (NP <- (PP <<, /by|via|through/ <2 (NP <+(NP) (NN=tr <, /ing$/))))");
    
    by5 = TregexPattern.compile("VP <-1  (PP <<, /by|via|through/ <2 (NP <+(NP) (NN=tr <, /ing$/)))");
    
    
    by6 = TregexPattern
            .compile("VP <-1 (PP <1 ADVP <2 (IN <<, /by|via|through/) <3 (S <1 (VP <1 VBG=tr)))");
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
    TregexMatcher m = by1.matcher(rightMostVp);
    if (m.find()) {
      matcher.trigger = m.getNode("tr");
      return true;
    }
    m = by2.matcher(rightMostVp);
    if (m.find()) {
      matcher.trigger = m.getNode("tr");
      return true;
    }
    m = by3.matcher(rightMostVp);
    if (m.find()) {
      matcher.trigger = m.getNode("tr");
      return true;
    }
    m = by4.matcher(rightMostVp);
    if (m.find()) {
      matcher.trigger = m.getNode("tr");
      return true;
    }    
    m = by5.matcher(rightMostVp);
    if (m.find()) {
        matcher.trigger = m.getNode("tr");
        return true;
      }
    m = by6.matcher(rightMostVp);
    if (m.find()) {
        matcher.trigger = m.getNode("tr");
        return true;
      }
    return false;
  }
}
