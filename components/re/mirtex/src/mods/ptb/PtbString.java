package mods.ptb;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Pair;

public class PtbString {

  public static final String BAR    = bar(1);

  public static final String END    = bar(2);

  public static final String MIDDLE = bar(3);

  private static String bar(int i) {
    try {
      switch (i) {
      case 1:
        return new String(new byte[] { -30, -108, -126 }, "utf8");
      case 2:
        return new String(new byte[] { -30, -108, -108 }, "utf8");
      case 3:
        return new String(new byte[] { -30, -108, -100 }, "utf8");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static int nameIndex;

  public static String tregexString(Tree root) {
    nameIndex = 0;
    return tregexStringRec(root).first;
  }

  /**
   * 
   * @param root
   * @param nameIndex
   * @return <pattern, root name>
   */
  private static Pair<String, String> tregexStringRec(Tree root) {
    StringBuilder sb = new StringBuilder();
    String name = "n" + nameIndex++;
    if (root.value().equals(",") || root.value().equals(":")) {
      sb.append(String.format("/%s/=%s", root.value(), name));
    } else {
      sb.append(String.format("%s=%s", root.value(), name));
    }

    if (root.numChildren() == 1 && root.getChild(0).isLeaf()) {
      ;
    } else {
      String lastName = "";
      for (int i = 0; i < root.numChildren(); i++) {
        Tree child = root.getChild(i);
        Pair<String, String> p = tregexStringRec(child);
        lastName = p.second;
        sb.append(String.format(" <%d (%s)", i + 1, p.first));
      }
      sb.append(" <- =" + lastName);
    }
    return new Pair<String, String>(sb.toString(), name);
  }

  public static String pennString(Tree root) {
    return pennString(
        root,
        Collections.<Tree> emptyList(),
        Collections.<String> emptyList());
  }

  public static String pennString(Tree root, List<Tree> highlightTrees,
      List<String> highlightNames) {
    StringBuilder sb = new StringBuilder();

    for (Tree child : root.preOrderNodeList()) {
      StringBuilder line = new StringBuilder();
      if (child.isLeaf()) {
        continue;
      }
      // add prefix
      for (Tree p = child.parent(root); p != null && p != root; p = p
          .parent(root)) {
        if (hasSiblings(p, root)) {
          line.insert(0, BAR + " ");
        } else {
          line.insert(0, "  ");
        }
      }
      if (child != root) {
        line.insert(0, "  ");
      }
      // if root has sibling node
      if (hasSiblings(child, root)) {
        line.append(MIDDLE + " ");
      } else {
        line.append(END + " ");
      }
      if (child.numChildren() == 1) {
        line.append(child.value());
        Tree leaf = child.firstChild();
        if (leaf.isLeaf()) {
          line.append(" " + child.firstChild().value());
        }
        int index = highlightTrees.indexOf(leaf);
        if (index != -1) {
          line.append("<---------" + highlightNames.get(index));
        }
      } else {
        line.append(child.value());
      }
      int index = highlightTrees.indexOf(child);
      if (index != -1) {
        line.append("<---------" + highlightNames.get(index));
      }
      line.append('\n');
      sb.append(line);
    }
    return sb.toString();
  }

  public static String pennString(Tree root, Tree trigger) {
    return pennString(
        root,
        Collections.singletonList(trigger),
        Collections.singletonList("trigger"));
  }

  public static String pennString(Tree root, Tree trigger, Tree theme) {
    return pennString(
        root,
        Arrays.asList(trigger, theme),
        Arrays.asList("trigger", "theme"));
  }

  private static boolean hasSiblings(Tree t, Tree root) {
    if (t == root) {
      return false;
    }
    return t.parent(root).lastChild() != t;
  }
}
