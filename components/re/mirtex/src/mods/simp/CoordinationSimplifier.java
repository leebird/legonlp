package mods.simp;

import java.util.List;

import mods.ptb.TOperationPattern;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

class CoordinationSimplifier extends Simplifier {

  static final TregexPattern cooPattern = TregexPattern
                                            .compile("__ < CC|CONJP");

  // static final TregexPattern leafPattern = TregexPattern
  // .compile("/NN/ <: /[^\\/]+[\\/-][^_]+/=leaf");
  // static final Pattern leafPatternStr = Pattern
  // .compile("([^/]+)[\\/-](.+)");

  protected CoordinationSimplifier(Tree root) {
    super(root);
  }

  @Override
  public boolean find() {
    // coordinate.txt
    List<SimplificationPattern> list = SimplificationTregexReader
        .getTregex(SimplificationTregexReader.Coordination);
    for (SimplificationPattern p : list) {
      TregexMatcher m2 = p.getTregexPattern().matcher(root);
      if (m2.find()) {
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

    TregexMatcher m = cooPattern.matcher(root);
    while (m.find()) {
      Tree coordination = m.getMatch();
      // isomorphic
      IsomorphicCoordination isomorphicCoordination = new IsomorphicCoordination(
          root, coordination);
      if (isomorphicCoordination.isCoordination()) {
        for (int i = 0; i < isomorphicCoordination.numberOfConjs(); i++) {
        	try {
        		Tree newTree = isomorphicCoordination.evaluate(i);
        		simplifiedTrees.add(newTree);
        	}
        	catch(Exception e) {
        		System.out.println(e);
        	}
        }
        return true;
      }
    }

    // split leaf
    // List<Tree> leaves = root.getLeaves();
    // for (int i = 0; i < leaves.size(); i++) {
    // Tree leaf = leaves.get(i);
    // if (!leaf.value().contains("_")) {
    // break;
    // }
    // if (!leaf.parent(root).value().equals("NN")
    // && !leaf.parent(root).value().equals("JJ")) {
    // continue;
    // }
    // Triple<String, Integer, Integer> value = PtbUtils
    // .parseValue(leaf.value());
    // // split /
    // String toks[] = value.first.split("/");
    // boolean b = splitLeaf(toks, value.second, i);
    // if (b) {
    // return true;
    // }
    //
    // // split -
    // toks = value.first.split("-");
    // boolean isUppercase = true;
    // for (String tok : toks) {
    // if (!Pattern.matches("[A-Z]{3,}", tok)) {
    // isUppercase = false;
    // }
    // }
    // if (isUppercase) {
    // b = splitLeaf(toks, value.second, i);
    // if (b) {
    // return true;
    // }
    // }
    // // split /[a-z]\d+[-][a-z]\d+/
    // if (Pattern.matches("[a-z]\\d+[-][a-z]\\d+", value.first)) {
    // b = splitLeaf(toks, value.second, i);
    // if (b) {
    // return true;
    // }
    // }
    // if (leaf.parent(root).value().equals("JJ")
    // && Pattern.matches("[A-Z]+\\d+[-][A-Z]+\\d+[A-Z]?", value.first)) {
    // b = splitLeaf2(toks, value.second, i);
    // if (b) {
    // return true;
    // }
    // }
    // }
    // NN CC NN NN

    // general
    m = cooPattern.matcher(root);
    while (m.find()) {
      Tree coordination = m.getMatch();
      GeneralCoordination generalCoordination = new GeneralCoordination(root,
          coordination);
      if (generalCoordination.isCoordination()) {
        for (int i = 0; i < generalCoordination.numberOfConjs(); i++) {
          Tree newTree = generalCoordination.evaluate(i);
          simplifiedTrees.add(newTree);
        }
        return true;
      }
    }
    // default
    m = cooPattern.matcher(root);
    while (m.find()) {
      Tree coordination = m.getMatch();
      DefaultCoordination defaultCoordination = new DefaultCoordination(root,
          coordination);
      if (defaultCoordination.isCoordination()) {
        for (int i = 0; i < defaultCoordination.numberOfConjs(); i++) {
          Tree newTree = defaultCoordination.evaluate(i);
          simplifiedTrees.add(newTree);
        }
        return true;
      }
    }

    return false;
  }

  // private boolean splitLeaf(String toks[], int from, int leafIndex) {
  // if (toks.length <= 1) {
  // return false;
  // }
  //
  // for (String tok : toks) {
  // if (tok.isEmpty()) {
  // return false;
  // }
  // }
  //
  // NounPhrase np1 = new NounPhrase(toks[0]);
  // for (int i = 1; i < toks.length; i++) {
  // if (NounPhraseSimilarity.compare(np1, new NounPhrase(toks[i])) <
  // NounPhraseSimilarity.HIGH) {
  // return false;
  // }
  // }
  //
  // for (String tok : toks) {
  // Tree t = root.deepCopy();
  // Tree subLeaf = t.getLeaves().get(leafIndex);
  // subLeaf.setValue(tok + "_" + from + "_" + (from + tok.length()));
  // from += tok.length() + 1;
  // simplifiedTrees.add(t);
  // }
  // return true;
  // }
  //
  // private boolean splitLeaf2(String toks[], int from, int leafIndex) {
  // if (toks.length <= 1) {
  // return false;
  // }
  //
  // for (String tok : toks) {
  // if (tok.isEmpty()) {
  // return false;
  // }
  // }
  //
  // for (String tok : toks) {
  // Tree t = root.deepCopy();
  // Tree subLeaf = t.getLeaves().get(leafIndex);
  // subLeaf.setValue(tok + "_" + from + "_" + (from + tok.length()));
  // from += tok.length() + 1;
  // simplifiedTrees.add(t);
  // }
  // return true;
  // }

  @Override
  public String type() {
    // TODO Auto-generated method stub
    return "coordination";
  }
}
