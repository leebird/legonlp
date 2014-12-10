package mods.extractor.pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ExtractorMatcher {

  ExtractorPattern pattern;
  Tree             tree;

  Tree             matched;
  Tree             trigger;
  Tree             argument;

  TregexMatcher    tregexMatcher;

  ExtractorMatcher(ExtractorPattern pattern, Tree tree) {
    this.pattern = pattern;
    this.tree = tree;
    reset();
  }

  private ExtractorMatcher reset() {
    matched = null;
    trigger = null;
    argument = null;
    tregexMatcher = pattern.getTregexPattern().matcher(tree);
    return this;
  }

  public boolean find() {
    return search();
  }

  private boolean search() {
    boolean result = tregexMatcher.find();

    boolean matched = false;

    if (result) {

    	matched = pattern.match(this, tregexMatcher, tree);
/*
      if (!matched) {
        matched = search();

      }
*/      
    }

    return matched;
  }

  public TregexPattern tregexPattern() {
    return pattern.getTregexPattern();
  }

  public ExtractorPattern pattern() {
    return pattern;
  }

  public Tree getMatch() {
    return matched;
  }

  public Tree trigger() {
    return trigger;
  }

  public Tree argument() {
    return argument;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ExtractorMatcher");
    sb.append("[pattern=" + pattern());
    sb.append(" lastmatch=");
    if (getMatch() != null) {
      sb.append(getMatch());
    }
    sb.append("]");
    return sb.toString();
  }
}
