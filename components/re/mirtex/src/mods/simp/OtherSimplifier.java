package mods.simp;

import java.util.ArrayList;
import java.util.List;

import mods.ptb.TOperationPattern;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

class OtherSimplifier extends Simplifier {

  private static final List<SimplificationPattern> list = new ArrayList<SimplificationPattern>();
  static {
    // // apposition
    // list.addAll(SimplificationTregexReader
    // .getTregex(SimplificationTregexReader.Apposition));
    //
    // // relative clause
    // list.addAll(SimplificationTregexReader
    // .getTregex(SimplificationTregexReader.RelativeClause));

    // sentence beginning
    list.addAll(SimplificationTregexReader
        .getTregex(SimplificationTregexReader.SentenceBeginning));

    // others
    list.addAll(SimplificationTregexReader
        .getTregex(SimplificationTregexReader.Others));
  }

  protected OtherSimplifier(Tree root) {
    super(root);
  }

  @Override
  public boolean find() {

    for (SimplificationPattern p : list) {
      if (p.getTregexPattern().toString()
          .startsWith("Root (NP=p < (NP=np1 $+ (PP <1 (JJ <: /such/ )")) {
        // System.err.println();
      }
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
    return "others";
  }

}
