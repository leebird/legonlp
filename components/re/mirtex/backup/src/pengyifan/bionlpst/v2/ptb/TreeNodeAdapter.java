package pengyifan.bionlpst.v2.ptb;
//package adapter;
//
//import pengyifan.util.TreeNode;
//import edu.stanford.nlp.ling.StringLabel;
//import edu.stanford.nlp.trees.LabeledScoredTreeNode;
//import edu.stanford.nlp.trees.Tree;
//
//public class TreeNodeAdapter {
//
//  public static TreeNode adapt(Tree t) {
//    // root
//    TreeNode tn = new TreeNode(t.value());
//    // child
//    for (Tree child : t.getChildrenAsList()) {
//      TreeNode childTn = adapt(child);
//      tn.add(childTn);
//    }
//    return tn;
//  }
//
//  public static Tree adapt(TreeNode n) {
//    // root
//    Tree t = new LabeledScoredTreeNode();
//    t.setLabel(new StringLabel((String) n.getObject()));
//    // child
//    for (TreeNode child : n.children()) {
//      Tree childT = adapt(child);
//      t.addChild(childT);
//    }
//    return t;
//  }
//}
