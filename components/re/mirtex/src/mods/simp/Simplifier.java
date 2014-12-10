package mods.simp;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;

public abstract class Simplifier {

  public static final int Parenthesis  = 1;
  public static final int Coordination = 2;
  public static final int Relative     = 3;
  public static final int Apposition   = 4;
  public static final int Others       = 6;

  protected Tree          root;
  protected List<Tree>    simplifiedTrees;

  public static Simplifier matcher(Tree root, int flags) {
    switch (flags) {
    case Parenthesis:
      return new ParenthesisSimplifier(root);
    case Coordination:
      return new CoordinationSimplifier(root);
    case Relative:
      return new RelativeClauseSimplifier(root);
    case Apposition:
      return new AppositionSimplifier(root);
    case Others:
      return new OtherSimplifier(root);
    default:
      return null;
    }
  }

  protected Simplifier(Tree root) {
    this.root = root;
    simplifiedTrees = new ArrayList<Tree>();
  }

  /**
   * Attempts to find the first node that matches the pattern.
   * 
   * @return
   */
  public abstract boolean find();

  public abstract String type();

  public final List<Tree> getSimplifiedTrees() {
    return simplifiedTrees;
  }
}
