package mods.extractor.pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ExtractorPattern {

    boolean       compiled;
    String        tregex;
    TregexPattern tregexPattern;
    String        name;
    String        argumentType;

  public ExtractorPattern(String tregex) {
    compiled = false;
    this.tregex = tregex;
    compile();
  }

  protected void compile() {
    compiled = true;
    tregexPattern = TregexPattern.compile(tregex);
  }

  public final ExtractorMatcher matcher(Tree tree) {
    if (!compiled) {
      compile();
    }
    return new ExtractorMatcher(this, tree);
  }

  public final TregexPattern getTregexPattern() {
    return tregexPattern;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

    public void setArgumentType(String type)
        {
            this.argumentType = type;
        }

    public String getArgumentType()
        {
            return this.argumentType;
        }

  protected boolean match(ExtractorMatcher matcher,
      TregexMatcher tregexMatcher, Tree tree) {

    matcher.matched = tregexMatcher.getNode("p");
    matcher.argument = tregexMatcher.getNode("arg");
    matcher.trigger = tregexMatcher.getNode("tr");

    return true;
  }

  @Override
  public final String toString() {
    return getName() + "[pattern=" + getTregexPattern().pattern() + "]";
  }
}
