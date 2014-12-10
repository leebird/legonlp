package mods.simp;

import java.util.List;

import mods.ptb.TOperationPattern;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

public class AppositionSimplifier extends Simplifier {

  public AppositionSimplifier(Tree root) {
    super(root);
  }

  @Override
  public boolean find() {

    List<SimplificationPattern> list = SimplificationTregexReader
        .getTregex(SimplificationTregexReader.Apposition);

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

    return false;
  }

  @Override
  public String type() {
    // TODO Auto-generated method stub
    return "apposition";
  }

}
