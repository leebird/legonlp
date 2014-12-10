package pengyifan.bionlpst.v3.extractor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.ArgumentType;
import pengyifan.bionlpst.v2.annotation.Event.EventType;
import pengyifan.bionlpst.v2.annotation.Token;
import pengyifan.bionlpst.v2.ptb.PtbReader;
import pengyifan.bionlpst.v2.ptb.PtbUtils;
import pengyifan.bionlpst.v3.extractor.pattern.BioNLPPatternFactory;
import pengyifan.bionlpst.v3.extractor.pattern.ExtractorMatcher;
import pengyifan.bionlpst.v3.extractor.pattern.ExtractorPattern;
import pengyifan.bionlpst.v3.extractor.pattern.PAPatternReader;
import pengyifan.bionlpst.v3.extractor.pattern.RightMostVp;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class Extractor extends FileProcessor {

  public static void main(String[] args)
      throws Exception {

    Extractor p = new Extractor(true, ALL);

    if (args.length == 0) {
      args = new String[] { "PMC-1447668-10-Discussion" };
      p.debug = true;
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  public static final int BASIC = 0;
  public static final int ALL   = 1;

  boolean                 isSimplify;
  int                     patternType;

  List<Entity>            a3ts;
  List<Event>             a3es;

  List<ExtractorPattern>  patterns;
  Treebank                treebank;

  public Extractor(boolean isSimplify, int patternType) {
    this.isSimplify = isSimplify;
    this.patternType = patternType;
    if (patternType == BASIC) {
      patterns = PAPatternReader.getBasicPatterns(
          Env.BASIC_PATTERN_TREGEX,
          BioNLPPatternFactory.instance());
    } else if (patternType == ALL)
      patterns = PAPatternReader.getPatterns(
          Env.BASIC_PATTERN_TREGEX,
          Env.NULL_PATTERN_TREGEX,
          BioNLPPatternFactory.instance());
  }

  @Override
  protected void readResource(String dir, String filename) {
    // read ptb
    PtbReader ptbReader = new PtbReader(dir + "/mccc/ptb2/" + filename + ".ptb");
    treebank = ptbReader.readTreebank();

    // simplify
    if (isSimplify) {
      ptbReader = new PtbReader(dir + "/mccc/ptb2/" + filename + ".ptb.simp");
      treebank.addAll(ptbReader.readTreebank());
    }

    // prune "in vivo"
    for (Tree t : treebank) {
      Ptbmods.Utils.prune(t);
    }

  }

  @Override
  public final void processFile(String dir, String filename) {

    super.processFile(dir, filename);

    // match
    a3es = new ArrayList<Event>();
    a3ts = new ArrayList<Entity>();

    for (Tree tree : treebank) {
      for (ExtractorPattern pattern : patterns) {
        if (pattern.getName().startsWith("Vnorm of ... with Arg")
            && tree.getLeaves().get(0).value().contains("Transactivation")) {
          // System.err.println();
        }
        ExtractorMatcher matcher = pattern.matcher(tree);
        while (matcher.find()) {
          Tree trigger = matcher.trigger();
          Tree argument = matcher.argument();

          if (trigger.value().equals("VP")) {
            trigger = RightMostVp.rightMostVp(trigger);
          }
          // np pp head
          argument = npppHead(argument);

          addEvent(tree, trigger, argument, "# " + matcher.pattern().toString());
        }
      }
    }

    // a3
    try {
      filename = dir + "/" + filename + ".a3";
      PrintStream out = new PrintStream(new FileOutputStream(filename));
      Collections.sort(a3es);
      for (Event e : a3es) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void addEvent(Tree root, Tree triggerTree, Tree argumentTree,
      String comment) {

    Entity trigger = findOrAddTrigger(root, triggerTree);
    Entity argument = findOrAddArgument(root, argumentTree);

    if (argument.from() == 1572) {
      // System.err.println();
    }

    if (!findEventInA3(trigger, argument)) {
      a3es.add(new Event(filename, EventType.Event, trigger,
          ArgumentType.Theme, argument, comment));
    }

  }

  protected Entity findOrAddTrigger(Tree root, Tree tree) {
    List<Token> tokens = Utils.getTokens(root, tree);
    Entity t = new Entity("", tree.value(), tokens);
    Entity foundT = findEntityInA3(t);
    if (foundT == null) {
      a3ts.add(t);
    }
    return t;
  }

  protected Entity findOrAddArgument(Tree root, Tree tree) {
    List<Token> tokens = Utils.getTokens(root, tree);
    Entity t = new Entity("", "NP", tokens);
    Entity foundT = findEntityInA3(t);
    if (foundT == null) {
      a3ts.add(t);
    }
    return t;
  }

  protected Entity findEntityInA3(Entity entity) {
    for (Entity e : a3ts) {
      if (e.equals(entity)) {
        return e;
      }
    }
    return null;
  }

  protected boolean findEventInA3(Entity trigger, Entity argument) {
    for (Event e : a3es) {
      if (e.trigger.equals(trigger) && e.argument.equals(argument)) {
        return true;
      }
    }
    return false;
  }

  static final TregexPattern p0 = TregexPattern
                                    .compile("NP <1 NP=np1 <2 (PP=pp <1 IN <2 NP=np2 <- =np2)");
  static final TregexPattern p1 = TregexPattern.compile("NP <1 NP=np1 <2 ADJP");
  static final TregexPattern p2 = TregexPattern
                                    .compile("NP <1 NP=np1 <2 /,/ <3 PP");

  // nppp head
  protected Tree npppHead(Tree t) {
    TregexMatcher m = p0.matcher(t);
    if (m.find() && t == m.getMatch()) {
      return m.getNode("np1");
    }
    m = p1.matcher(t);
    if (m.find() && t == m.getMatch()) {
      return m.getNode("np1");
    }
    m = p2.matcher(t);
    if (m.find() && t == m.getMatch()) {
      return m.getNode("np1");
    }
    return t;
  }
}
