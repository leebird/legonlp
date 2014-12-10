package mods.extractor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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
import mods.extractor.pattern.PAPatternReader;
import mods.extractor.pattern.RightMostVp;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class Extractor extends FileProcessor {

  public static void main(String[] args)
      throws Exception {

    Extractor p = new Extractor(true, ALL_SPLIT);

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
    public static final int ALL_SPLIT = 2;


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
    else if (patternType == ALL_SPLIT)
        patterns = PAPatternReader.getPatterns(
            Env.AGENT_PATTERN_TREGEX,
            Env.THEME_PATTERN_TREGEX,
            Env.NULL_PATTERN_TREGEX,
            BioNLPPatternFactory.instance());
  }
    
  @Override
  protected void readResource(String dir, String filename) {
      // read ptb
      PtbReader ptbReader = new PtbReader(Env.DIR_SIMP + filename + ".ptb");
      treebank = ptbReader.readTreebank();

    // simplify
    if (isSimplify) {
      ptbReader = new PtbReader(Env.DIR_SIMP + filename + ".ptb.simp");
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
    a3es = new ArrayList<Event>();
    a3ts = new ArrayList<Entity>();

    for (Tree tree : treebank) {
    	
      for (ExtractorPattern pattern : patterns) {

        ExtractorMatcher matcher = pattern.matcher(tree);

        
        while (matcher.find()) {
          Tree trigger = matcher.trigger();
          Tree argument = matcher.argument();

          if (trigger.value().equals("VP")) {
              trigger = RightMostVp.rightMostVp(trigger);
          }          
          
          //Entity triggerT = findOrAddTrigger(tree, trigger);
          
          //System.out.println(pattern.getArgumentType());
          //System.out.println(pattern.getName());
          //System.out.println(triggerT.tokens.getLast().word);

          // np pp head

          argument = npppHead(argument);

          addEvent(tree, trigger, argument, matcher.pattern().getArgumentType(),"# " + matcher.pattern().toString());
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

  private void addEvent(Tree root, Tree triggerTree, Tree argumentTree,String argumentType,
      String comment) {

	  if(triggerTree.isLeaf() || argumentTree.isLeaf())
		  return;
	  
	Entity trigger = findOrAddTrigger(root, triggerTree);
	
    Entity argument = findOrAddArgument(root, argumentTree);

    Event event = new Event(filename, EventType.Event, trigger,
            ArgumentType.getEnum(argumentType), argument, comment);
    
    if (!findEventInA3(event)) {
      a3es.add(event);
    }

  }

  protected Entity findOrAddTrigger(Tree root, Tree tree) {
    List<Token> tokens = Utils.getTokens(root, tree);
    
    //Entity t = new Entity("", tree.value(), tokens);

    Entity t = new Entity("", "Trigger", tokens);
    
   
    Entity foundT = findEntityInA3(t);
    if (foundT == null) {
      a3ts.add(t);
    }
    return t;
  }

  protected Entity findOrAddArgument(Tree root, Tree tree) {
    List<Token> tokens = Utils.getTokens(root, tree);
    Entity t = new Entity("", "Argument", tokens);

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

  // compare event directly instead of using argument and trigger
  protected boolean findEventInA3(Event event) {
	    for (Event e : a3es) {
	      if (e.equals(event)) {
	        return true;
	      }
	    }
	    return false;
	  }
  
  static final TregexPattern p0 = TregexPattern
                                    .compile("NP <1 NP=np1 <2 (PP=pp <1 (TO|IN << !/^(of|from)$/) <2 NP=np2 <- =np2)");
  
  static final TregexPattern p01 = TregexPattern
          .compile("NP <1 NP=np1 <2 (PP=pp <1 (IN << /^(of|from)$/))");
  
  static final TregexPattern p1 = TregexPattern.compile("NP <1 NP=np1 <2 ADJP");
  
  static final TregexPattern p2 = TregexPattern
                                    .compile("NP <1 NP=np1 <2 /,/ <3 PP");

  static final TregexPattern p3 = TregexPattern.compile("NP <1 NP=np1 << SBAR");
  
  // nppp head
  protected Tree npppHead(Tree t) {
	  
    TregexMatcher m = p0.matcher(t);
    if (m.find() && t == m.getMatch()) {

      return m.getNode("np1");
    }
    
    m = p01.matcher(t);

    if (m.find() && t.getLeaves().get(0) == m.getMatch().getLeaves().get(0)) {

       Tree np1 = m.getNode("np1");
       Tree pp1 = m.getNode("pp");
       Tree res = np1.deepCopy();
       for(Tree child : pp1.children())
       {
    	   // if SBAR in NP phrase, stop adding children
    	   if(child.toString().contains("SBAR"))
    		   break;
    	   res.addChild(child);       
       }
       return res;
    }
    
    m = p1.matcher(t);
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
}
