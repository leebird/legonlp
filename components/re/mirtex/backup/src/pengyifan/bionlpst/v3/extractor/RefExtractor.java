package pengyifan.bionlpst.v3.extractor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
import pengyifan.bionlpst.v3.extractor.pattern.RefPatternReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class RefExtractor extends FileProcessor {

  public static void main(String[] args)
      throws Exception {

    RefExtractor p = new RefExtractor();

    // args = new String[] { "PMID-8657101" };

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  boolean                isSimplify;

  List<Event>            refes;

  List<ExtractorPattern> patterns;
  Treebank               treebank;
  ExtractorPattern       currentPattern;

  public RefExtractor() {
    isSimplify = true;
    patterns = RefPatternReader.getPatterns(
        Env.REFEXTRACTOR_TREGEX,
        Env.MEMBER_COLLECTION_TREGEX,
        Env.PART_WHOLE_TREGEX,
        BioNLPPatternFactory.instance());
  }

  @Override
  protected void readResource(String dir, String filename) {
    // read ptb
    PtbReader ptbReader = new PtbReader(dir + "/mccc/ptb2/" + filename + ".ptb");
    treebank = ptbReader.readTreebank();

    // simplify
    if (isSimplify) {
      ptbReader = new PtbReader(dir
          + "/mccc/ptb2/"
          + filename
          + ".ptb.simp.parcoo");
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
    refes = new ArrayList<Event>();

    for (Tree tree : treebank) {
      for (ExtractorPattern pattern : patterns) {
        currentPattern = pattern;

        if (pattern.getName().equals("apposition")) {
          // System.err.println();
        }
        ExtractorMatcher matcher = pattern.matcher(tree);
        while (matcher.find()) {
          Tree trigger = matcher.trigger();
          Tree argument = matcher.argument();

          // nppp
          trigger = npppHead(trigger);
          argument = npppHead(argument);

          // assert trigger != null : matcher;
          addEvent(tree, trigger, argument, "# " + matcher.pattern().toString());
        }
      }
    }

    // a3
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/ref/"
          + filename
          + ".ref"));
      Collections.sort(refes);
      for (Event e : refes) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      System.err.println(e.getMessage());
      // System.exit(1);
    }
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

  private void addEvent(Tree root, Tree triggerTree, Tree argumentTree,
      String comment) {

    Entity trigger = tree2entity(root, triggerTree);
    Entity argument = tree2entity(root, argumentTree);

    if (argument.from() == 459) {
      // System.err.println();
    }

    List<Entity> arguments = new LinkedList<Entity>();
    arguments.add(argument);
    // split in
    int indexIn = getInIndex(argument.tokens);
    if (indexIn != -1) {
      argument = new Entity(argument.id, argument.type,
          argument.tokens.subList(0, indexIn));
      arguments.add(argument);
    }

    for (Entity arg : arguments) {
      if (!findEventInA3(trigger, arg)) {
        refes.add(new Event(filename, EventType.Event, trigger,
            ArgumentType.Coreference, arg, comment));
      }
    }
  }

  protected int getInIndex(List<Token> tokens) {
    for (int i = 1; i < tokens.size(); i++) {
      if (tokens.get(i).pos.equals(",")) {
        return i;
      }
    }
    return -1;
  }

  protected Entity tree2entity(Tree root, Tree tree) {
    try {
      List<Token> tokens = Utils.getTokens(root, tree);
      Entity t = new Entity("", "NP", tokens);
      return t;
    } catch (NullPointerException e) {
      System.err.println(filename);
      System.err.println(currentPattern);
      System.err.println(tree);
      System.exit(1);
    }
    return null;
  }

  protected boolean findEventInA3(Entity trigger, Entity argument) {
    for (Event e : refes) {
      if (e.trigger.equals(trigger) && e.argument.equals(argument)) {
        return true;
      }
    }
    return false;
  }
}
