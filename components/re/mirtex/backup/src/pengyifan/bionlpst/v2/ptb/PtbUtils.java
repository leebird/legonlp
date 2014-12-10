package pengyifan.bionlpst.v2.ptb;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;

public class PtbUtils {

  private static TregexPattern   inVivo1   = TregexPattern
                                               .compile("ADJP=p <1 FW <2 FW");
  private static TregexPattern   inVivo2   = TregexPattern
                                               .compile("PP=p <1 (IN <<: /in/) <2 (NP <<: /vitro/)");
  private static TsurgeonPattern inVivoOpt = Tsurgeon.parseOperation("prune p");

  public static void prune(Tree tree) {
    TregexMatcher m = inVivo1.matcher(tree);
    while (m.find()) {
      inVivoOpt.evaluate(tree, m);
    }
    m = inVivo2.matcher(tree);
    while (m.find()) {
      inVivoOpt.evaluate(tree, m);
    }
  }




}
