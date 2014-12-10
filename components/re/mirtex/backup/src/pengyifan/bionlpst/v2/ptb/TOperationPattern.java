package pengyifan.bionlpst.v2.ptb;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonParseException;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;

/**
 * no collectOperations provided
 */
public class TOperationPattern {

  static class NewNode {

    String       operator;
    List<String> args;

    NewNode() {
      args = new ArrayList<String>();
    }

    public static NewNode parseOperation(String pattern) {
      String toks[] = pattern.split(" +");
      NewNode newNode = new NewNode();
      newNode.operator = "new";
      for (int i = 1; i < toks.length; i++) {
        newNode.args.add(toks[i]);
      }
      return newNode;
    }

    public Tree evaluate(Tree t, TregexMatcher matcher) {
      while (t.numChildren() != 0) {
        t.removeChild(0);
      }
      for (String arg : args) {
        Tree child = matcher.getNode(arg);
        t.addChild(child);
      }
      return t;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("[operations: (new(");
      for (int i = 0; i < args.size(); i++) {
        sb.append(args.get(i));
        if (i != args.size() - 1) {
          sb.append(", ");
        }
      }
      sb.append("))]");
      return sb.toString();
    }
  }

  List<Object> operations;

  private TOperationPattern() {
    operations = new ArrayList<Object>();
  }

  public static TOperationPattern parseOperation(String line) {
    String operations[] = line.split("[,]+");
    TOperationPattern top = new TOperationPattern();
    for (String operation : operations) {
      String toks[] = operation.trim().split("[ ]+");
      if (toks[0].equals("prune")
          || toks[0].equals("replace")
          || toks[0].equals("insert")
          || toks[0].equals("move")
          || toks[0].equals("relabel")) {
        top.operations.add(Tsurgeon.parseOperation(operation));
      } else if (toks[0].equals("new")) {
        top.operations.add(NewNode.parseOperation(operation));
      } else {
        throw new TsurgeonParseException("Error parsing Tsurgeon expression: "
            + toks[0]
            + ":"
            + line, null);
      }
    }
    return top;
  }

  public Tree evaluate(Tree t, TregexPattern pattern) {
    Tree newT = t.deepCopy();
    TregexMatcher m = pattern.matcher(newT);
    if (!m.find()) {
      throw new IllegalArgumentException("no matching found:"
          + pattern
          + "\n"
          + t);
    }
    for (Object obj : operations) {
      if (obj instanceof TsurgeonPattern) {
        TsurgeonPattern p = (TsurgeonPattern) obj;
        p.evaluate(newT, m);
      } else if (obj instanceof NewNode) {
        NewNode p = (NewNode) obj;
        p.evaluate(newT, m);
      }
    }

    return newT;
  }

  @Override
  public String toString() {
    return operations.toString();
  }
}
