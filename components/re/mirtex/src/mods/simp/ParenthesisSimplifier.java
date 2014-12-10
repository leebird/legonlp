package mods.simp;

import java.util.List;

import mods.ptb.TOperationPattern;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

class ParenthesisSimplifier extends Simplifier {

  static final TregexPattern               lrbPattern = TregexPattern
                                                          .compile("-LRB-=n0 <: /-LRB-|-LSB-/");

  static final TregexPattern               rrbPattern = TregexPattern
                                                          .compile("-RRB-=n0 <: /-RRB-|-RSB-/");

  static final TregexPattern               prnPattern = TregexPattern
                                                          .compile("PRN=n0");
  
  static final TregexPattern               leafPattern = TregexPattern
      .compile("/[^-]+-LRB-[^-]+-RRB-/");
  
  static final TOperationPattern           prune      = TOperationPattern
                                                          .parseOperation("prune n0");

  static final List<SimplificationPattern> list       = SimplificationTregexReader
                                                          .getTregex(SimplificationTregexReader.ParentThesis);

  protected ParenthesisSimplifier(Tree root) {
    super(root);
  }

  @Override
  public boolean find() {
    // parenthesis.txt
    for (SimplificationPattern p : list) {
      TregexMatcher m = p.getTregexPattern().matcher(root);
      if (m.find()) {
        for (TOperationPattern oPattern : p.getOperations()) {
          try {
            Tree newTree = oPattern.evaluate(root, p.getTregexPattern());
            simplifiedTrees.add(newTree);
          } catch (NullPointerException e) {
            System.err.println(p);
            e.printStackTrace();
            System.exit(1);
          }
        }
        return true;
      }
    }
    // default
    TregexMatcher m = prnPattern.matcher(root);
    if (m.find()) {
      Tree newTree = prune.evaluate(root, prnPattern);
      simplifiedTrees.add(newTree);
      return true;
    }
    m = lrbPattern.matcher(root);
    if (m.find()) {
      Tree newTree = prune.evaluate(root, lrbPattern);
      simplifiedTrees.add(newTree);
      return true;
    }
    m = rrbPattern.matcher(root);
    if (m.find()) {
      Tree newTree = prune.evaluate(root, rrbPattern);
      simplifiedTrees.add(newTree);
      return true;
    }
    return false;
  }

  @Override
  public String type() {
    // TODO Auto-generated method stub
    return "parenthesis";
  }

}
