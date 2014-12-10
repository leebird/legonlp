package pengyifan.bionlpst.v3.simp;

import java.util.LinkedList;
import java.util.List;

import pengyifan.bionlpst.v2.Utils;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.Pair;

public abstract class Coordination {

  protected static final TregexPattern      conjPattern = TregexPattern
                                                            .compile("__ < CC|CONJP");
  protected LinkedList<Pair<Integer, Tree>> conjuncts;
  protected Tree                            root;
  protected Tree                            coordination;

  public Coordination(Tree root, Tree coordination) {
    this.root = root;
    this.coordination = coordination;
    conjuncts = new LinkedList<Pair<Integer, Tree>>();
  }

  public abstract boolean isCoordination();

  protected boolean isCC(Tree t) {
    return t.value().equals("CC") || t.value().equals("CONJP");
  }

  protected boolean isComma(Tree t) {
    return t.value().equals(",")
        || t.value().equals(".")
        || t.value().equals(";")
        || t.value().equals(":");
  }

  public int numberOfConjs() {
    return conjuncts.size();
  }

  public Tree evaluate(int indexOfConj) {
    Tree newT = root.deepCopy();
    Tree newCoor = null;
    TregexMatcher m = conjPattern.matcher(newT);
    while (m.find()) {
      if (isEqual(m.getMatch())) {
        newCoor = m.getMatch();
        break;
      }
    }
    assert newCoor != null;

    Tree pParent = newCoor.parent(newT);
    assert pParent != null : newT;

    List<Tree> daughters = newCoor.parent(newT).getChildrenAsList();
    int index = daughters.indexOf(newCoor);

    assert newCoor.numChildren() > conjuncts.get(indexOfConj).first : newCoor;
    pParent.setChild(index, newCoor.getChild(conjuncts.get(indexOfConj).first));

    return newT;
  }

  private boolean isEqual(Tree newCoor) {
    Tree newRightMostLeaf = Utils.lastLeaf(newCoor);
    Tree rightMostLeaf = Utils.lastLeaf(coordination);
    if (!newRightMostLeaf.value().equals(rightMostLeaf.value())) {
      return false;
    }
    Tree newLeftMostLeaf = Utils.firstLeaf(newCoor);
    Tree leftMostLeaf = Utils.firstLeaf(coordination);
    if (!newLeftMostLeaf.value().equals(leftMostLeaf.value())) {
      return false;
    }

    return true;
  }

}
