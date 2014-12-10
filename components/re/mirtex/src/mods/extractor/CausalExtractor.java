package mods.extractor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Event.ArgumentType;
import mods.annotation.Event.EventType;
import mods.annotation.Token;
import mods.ptb.PtbReader;
import mods.ptb.PtbUtils;
import mods.extractor.pattern.BioNLPPatternFactory;
import mods.extractor.pattern.ExtractorMatcher;
import mods.extractor.pattern.ExtractorPattern;
import mods.extractor.pattern.CausalPatternReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class CausalExtractor extends FileProcessor {

  public static void main(String[] args)
      throws Exception {

    RefExtractor p = new RefExtractor();

    // args = new String[] { "PMID-8657101" };

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR_REF, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  boolean                isSimplify;

  List<Event>            refes;

  List<ExtractorPattern> patterns;
  Treebank               treebank;
  ExtractorPattern       currentPattern;

  public CausalExtractor() {
    isSimplify = true;
    patterns = CausalPatternReader.getPatterns(
        Env.CAUSE_TREGEX,
        Env.EFFECT_TREGEX,
        BioNLPPatternFactory.instance());
  }

  @Override
  protected void readResource(String dir, String filename) {
    // read ptb
    PtbReader ptbReader = new PtbReader(Env.DIR_SIMP + filename + ".ptb");
    treebank = ptbReader.readTreebank();

    // simplify
    if (isSimplify) {
      ptbReader = new PtbReader(Env.DIR_SIMP + filename
          + ".ptb.simp");
      treebank.addAll(ptbReader.readTreebank());
    }

    // prune "in vivo"
    for (Tree t : treebank) {
      PtbUtils.prune(t);
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

        ExtractorMatcher matcher = pattern.matcher(tree);
        while (matcher.find()) {
          Tree trigger = matcher.trigger();
          Tree argument = matcher.argument();

          // nppp
          trigger = npppHead(trigger);
          argument = npppHead(argument);

          // assert trigger != null : matcher;
          addEvent(tree, trigger, argument, pattern.getArgumentType(), "# " + matcher.pattern().toString());
        }
      }
    }

    // a3
    try {
      PrintStream out = new PrintStream(new FileOutputStream(Env.DIR_REF
          + filename
          + ".causal"));
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
/*
  static final TregexPattern p0 = TregexPattern
                                    .compile("NP <1 NP=np1 <2 (PP=pp <1 IN <2 NP=np2 <- =np2)");
  static final TregexPattern p1 = TregexPattern.compile("NP <1 NP=np1 <2 ADJP");
  static final TregexPattern p2 = TregexPattern
                                    .compile("NP <1 NP=np1 <2 /,/ <3 PP");
*/
  
  static final TregexPattern p0 = TregexPattern
          .compile("NP <1 NP=np1 <2 (PP=pp <1 (TO|IN << !/^of$/) <2 NP=np2 <- =np2)");

  static final TregexPattern p01 = TregexPattern
		  .compile("NP <1 NP=np1 <2 (PP=pp <1 (IN << /^of$/))");

  static final TregexPattern p1 = TregexPattern.compile("NP <1 NP=np1 <2 ADJP");

  static final TregexPattern p2 = TregexPattern
		  .compile("NP <1 NP=np1 <2 /,/ <3 PP");
  
  static final TregexPattern p3 = TregexPattern.compile("NP <1 NP=np1 < SBAR");
  
/*
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
*/

protected Tree npppHead(Tree t) {
	  // when extracting causal relation, do not use head only, because the arugment can be an event itself                 
//    TregexMatcher m = p0.matcher(t);
//    if (m.find() && t == m.getMatch()) {
//
//      return m.getNode("np1");
//    }
//    
//    m = p01.matcher(t);
//
//    if (m.find() && t.getLeaves().get(0) == m.getMatch().getLeaves().get(0)) {
//
//       Tree np1 = m.getNode("np1");
//       Tree pp1 = m.getNode("pp");
//       Tree res = np1.deepCopy();
//       for(Tree child : pp1.children())
//       {
//    	   res.addChild(child);       
//       }
//       return res;
//    }
    
	TregexMatcher m = p1.matcher(t);
    if (m.find() && t == m.getMatch()) {
    	
      return m.getNode("np1");
    }
    m = p2.matcher(t);
    if (m.find() && t == m.getMatch()) {
    	
      return m.getNode("np1");
    }
    m = p3.matcher(t);
    if (m.find() && t == m.getMatch()) {
    	
      return m.getNode("np1");
    }
    return t;

  }
  private void addEvent(Tree root, Tree triggerTree, Tree argumentTree, String argumentType,
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
            Event.ArgumentType.getEnum(argumentType), arg, comment));
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
