package mods.extractor.pattern;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import java.util.List;
import java.util.ArrayList;

public class ArgVadj extends ExtractorPattern {

  public ArgVadj(String tregex) {
    super(tregex);
  }

  List<TregexPattern> vadjs;
  TregexPattern vadj;

  @Override
  public String getName() {
    return "Arg Vadj";
  }

  @Override
  protected void compile() {
    super.compile();
    //vadj = TregexPattern.compile("VP <1 /VB.*/ <2 (ADJP=tr)");
    
    // a is JJ
    vadjs = new ArrayList<TregexPattern>();
    vadj = TregexPattern.compile("VP <1 AUX < ADJP|VP=tr");
    vadjs.add(vadj);
    //vadj = TregexPattern.compile("VP <1 AUX < ADVP < ADJP|VP=tr");
    //vadjs.add(vadj);
    vadj = TregexPattern.compile("VP=tr <1 VBN");
    vadjs.add(vadj);
    vadj = TregexPattern.compile("VBN|JJ=tr");
    vadjs.add(vadj);
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

	  for(TregexPattern vadj : vadjs)
	  {
		  // check VP matches Vadj patterns
		  TregexMatcher m = vadj.matcher(matcher.trigger);
		  if (m.find() && matcher.trigger == m.getMatch()) {
			  matcher.trigger = getLastJJ(m.getNode("tr"));
			  return true;
		  }

		  // check VP rightmost child matches Vadj patterns
		  Tree rightMostVp = RightMostVp.rightMostVp(matcher.trigger);

		  m = vadj.matcher(rightMostVp);
		  if (m.find() && rightMostVp == m.getMatch()) {
			  matcher.trigger = getLastJJ(m.getNode("tr"));
			  return true;
		  }

		  // check VP rightmost child's children matches Vadj patterns
		  for (Tree child : rightMostVp.children()) {
			  m = vadj.matcher(child);
			  
			  if (m.find() && child == m.getMatch()) {
				  matcher.trigger = getLastJJ(m.getNode("tr"));
				  return true;
			  }
		  }
	  }

	  return false;
  }

  private Tree getLastJJ(Tree tree) {
    // last JJ
    for (int i = tree.numChildren() - 1; i >= 0; i--) {
      Tree child = tree.getChild(i);
      if (child.value().equals("JJ") || child.value().equals("VBN")) {
        return child;
      }
    }
    return tree;
  }

}
