package mods.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import mods.ptb.OffsetLabel;
import mods.ptb.PtbReader;
import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import mods.annotation.A3EventReader;
import mods.annotation.Event;
import mods.annotation.Entity;
import mods.annotation.Event.EventType;
import mods.annotation.Event.ArgumentType;
import mods.annotation.A1EntityReader;
import mods.annotation.Token;

public class ResoluteAnaphora extends FileProcessor {

  @Override
  public void processFile(String dir, String filename) {
    super.processFile(dir, filename);
    // generate target file
    printA4(dir, filename);
  }

  public String agentType = "";
  public String themeType = "";
  
  public ResoluteAnaphora(String at, String tt)
  {
	  agentType = at;
	  themeType = tt;
  }
  
  protected void printA4(String dir, String filename) {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/"
          + filename
          + ".a3.Anaphora"));
      
      // get all the parse trees
      PtbReader ptbReader = new PtbReader(Env.DIR_SIMP + filename + ".ptb");
      Treebank treebank = ptbReader.readTreebank();
      
      // get all the entities in a1 file
      List<Entity> a1ts = A1EntityReader.readEntities(dir + "/" + filename + ".a1");
      
      // read ENTITY-string and entity mapping
      String mapText = dir + "/" + filename + ".entmap";
      File file = new File(mapText);
      HashMap<String,HashMap<String,String>> map = new HashMap<String,HashMap<String,String>>();
      try {
      	String mapstr = FileUtils.readFileToString(file);
      	Gson gson = new Gson();
      	Type mapType = new TypeToken<HashMap<String,HashMap<String,String>>>() {}.getType();
      	map = gson.fromJson(mapstr, mapType);
      } catch (Exception e) {
      	System.exit(1);
      }
      
      // get all the events in a3 files
      List<Event> a3es = new ArrayList<Event>();
      
      // resolved events
      List<Event> resolvedEvents = new ArrayList<Event>();
      
      for (EventType type : EventType.values()) {
        File a3file = new File(dir + "/" + filename + ".a3." + type);
        if (!a3file.exists()) {
          continue;
        }
        a3es.addAll(A3EventReader.readEvents(dir + "/" + filename + ".a3." + type));
      }
      
      for(Event e : a3es)
      {
    	  Entity argument = e.argument;
    	  String first = argument.getFirst().word;
    	  String last = argument.getLast().word;
    	  String whole = argument.getText().toLowerCase();
    	  
    	  List<Entity> candidates = new ArrayList<Entity>();
    	  String argumentType = "";
    	  int lowerLimit = 0;
    	  int upperLimit = 0;
    	  
    	  if(e.argumentType == ArgumentType.Agent)
    		  argumentType = agentType;
    	  else if(e.argumentType == ArgumentType.Theme)
    		  argumentType = themeType;
    	  else
    		  continue;
    	  
    	  String anaphoraType = "NonTypedAnaphora";
    	  
    	  if (isAnaphora(argument, a1ts, map)) {
    		  
    		  // if stop at the first find
    		  boolean greedy = true;
    		  
    		  // solve a few simple cases for now
    		  if (first.equalsIgnoreCase("its")
    			  || first.equalsIgnoreCase("it")) {
    			  
        		  // get candidates
        		  candidates = getCandidates(argument, treebank, true);
        		  lowerLimit = 0;
        		  upperLimit = 2;
        		  greedy = false;
    		  }

    		  if (first.equalsIgnoreCase("they")
    				  || first.equalsIgnoreCase("them")
    				  || first.equalsIgnoreCase("their")) {
    			  
    			  // get candidates
        		  candidates = getCandidates(argument, treebank, false);

        		  lowerLimit = 1;
        		  upperLimit = 200;
        		  greedy = false;
    		  }

    		  if (first.equalsIgnoreCase("those")
    				  || first.equalsIgnoreCase("these")) {
    			  
    			  if(!last.equalsIgnoreCase(first))
    			  {
    				  if(last.equalsIgnoreCase("proteins") ||
    						  last.equalsIgnoreCase("genes"))
    					  {
    					  	if(!argumentType.equals("Gene"))
    					  		continue;
    					  }
    				  else if((last.equalsIgnoreCase("miRNAs") ||
    						  last.equalsIgnoreCase("miRs") ||
    						  last.equalsIgnoreCase("microRNAs")))
    					  {
    					  	if(!argumentType.equals("MiRNA"))
    					  		continue;
    					  }
    				  else
    					  continue;
    			  }
    			  anaphoraType = "TypedAnaphora";
    			  candidates = getCandidates(argument, treebank, false);
        		  lowerLimit = 1;
        		  upperLimit = 200;
        		  greedy = true;
    		  }
    	
    		  if (first.equalsIgnoreCase("both") || whole.indexOf("both") > -1) {
    			  boolean isBoth = false;
    			  if(whole.indexOf("both proteins") > -1 ||
    					  whole.indexOf("both genes") > -1 ||
    					  whole.indexOf("both mirnas") > -1 ||
    					  whole.indexOf("both mirna") > -1 ||
    					  whole.indexOf("both mirs") > -1 ||
    					  whole.indexOf("both microrna") > -1)
    				  isBoth = true;
    			  
    			  if(!isBoth && !last.equalsIgnoreCase(first))
    			  {
    				  if(last.equalsIgnoreCase("proteins") ||
    						  last.equalsIgnoreCase("genes"))
    					  {
    					  	if(!argumentType.equals("Gene"))
    					  		continue;
    					  }
    				  else if((last.equalsIgnoreCase("miRNAs") ||
    						  last.equalsIgnoreCase("miRs") ||
    						  last.equalsIgnoreCase("microRNAs")))
    					  {
    					  	if(!argumentType.equals("MiRNA"))
    					  		continue;
    					  }
    				  else
    					  continue;
    			  }
    			  anaphoraType = "TypedAnaphora";
    			  candidates = getCandidates(argument, treebank, false);
        		  lowerLimit = 1;
        		  upperLimit = 3;
        		  greedy = false;
    		  }
    		  
    		  if (first.equalsIgnoreCase("the")) {
    
    			  String patpp = "(^|\\s)(in|of|by|to|on|from|after|before)\\s";
    			  Pattern r = Pattern.compile(patpp,Pattern.CASE_INSENSITIVE);
    			  Matcher m = r.matcher(whole);
    			  if(m.find())
    				  continue;
				  
    			  if(last.equalsIgnoreCase("proteins") ||
						  last.equalsIgnoreCase("genes"))
					  {
					  	if(!argumentType.equals("Gene"))
					  		continue;
					  }
				  else if((last.equalsIgnoreCase("miRNAs") ||
						  last.equalsIgnoreCase("miRs") ||
						  last.equalsIgnoreCase("microRNAs")))
					  {
					  	if(!argumentType.equals("MiRNA"))
					  		continue;
					  }
				  else
					  continue;
    			  
    			  anaphoraType = "TypedAnaphora";
    			  
    			  if(last.endsWith("s")) {
        			  candidates = getCandidates(argument, treebank, false);
        			  lowerLimit = 1;
        			  upperLimit = 100;
        			  greedy = false;
    			  }
    			  else
    			  {
        			  candidates = getCandidates(argument, treebank, false);
        			  lowerLimit = 0;
        			  upperLimit = 2;
        			  greedy = false;
    			  }
    		  }

    		  Collections.sort(candidates);
    		  Collections.reverse(candidates);
    		  if(candidates.size() > 0)
    		  {
    			  
    			  int start = candidates.get(0).from();
    			  int end = candidates.get(0).to();
     			  int antecedent = 0;
    			  for (Entity c : candidates) {
    				  
    				  int count = countEntity(c, a1ts, map, argumentType);

    				  if (count > lowerLimit && count < upperLimit)
    				  {
    					  
    					  // if more than 1 candidate and not greedy, stop
    					  // start and end are used to catch NNs/NPs in NP, this situation is excluded for non-greedy
    					  if(!greedy && (c.from() < start) && (c.to() < end) && (antecedent > 0))
    						  break;
    					  antecedent++;
    					  start = c.from();
    					  end = c.to();
    					  resolvedEvents.add(new Event(filename, e.type,
    							  			e.trigger, e.argumentType, c,
    							  			e.comment+" - "+anaphoraType));   					  
    				  }
    			  }	  
    		  }
    	  }
      }

	  //System.out.println(resolvedEvents);
     // System.out.println(resolvedEvents);
      for(Event e : resolvedEvents)
      {
    	  out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  // count how many entities in a candidate
  protected int countEntity(Entity entity, List<Entity> entityList, String argumentType)
  {
	  int count = 0;
	  
	  for(Entity e : entityList)
	  {
		  if(entity.range().containsRange(e.range()))
		  {
			  if(argumentType.equals("Gene"))
			  {
				  if(e.type.equals("Gene") || e.type.equals("Family") || e.type.equals("Complex"))
					  count++;
			  }
			  else if(e.type.equals(argumentType))
				  count++;
			  else
				  continue;
		  }
	  }
	  return count;
  }
  
  // count how many entities in a candidate
  protected int countEntity(Entity entity, List<Entity> entityList, HashMap<String,HashMap<String,String>> map, String argumentType)
  {
	  int count = 0;
	  
	  for(Entity e : entityList)
	  {
		  if(entityInArgument(e,entity,map))
		  {
			  if(argumentType.equals("Gene"))
			  {
				  if(e.type.equals("Gene") || e.type.equals("Family") || e.type.equals("Complex"))
					  count++;
			  }
			  else if(e.type.equals(argumentType))
				  count++;
			  else
				  continue;
		  }
	  }
	  return count;
  }
  
  // check if it's really an anaphora
  protected boolean isAnaphora(Entity entity, List<Entity> entityList, HashMap<String,HashMap<String,String>> map) {
	  for(Entity e : entityList)
	  {
		  if(entityInArgument(e,entity,map))
			  return false;
	  }
	  return true;
  }
  
  // get antecedent candidates for an anaphora
  protected List<Entity> getCandidates(Entity entity, Treebank treebank, boolean intraSentence)
  {
	  List<Entity> entityList = new ArrayList<Entity>();
	  
	  for(Tree tree : treebank)
	  {
		  List<Tree> leaves = tree.getLeaves();
		  OffsetLabel first = (OffsetLabel) leaves.get(0).label();
		  OffsetLabel last = (OffsetLabel) leaves.get(leaves.size() - 1).label();
		  int start = first.beginPosition();
		  int end = last.endPosition();
		  
		  TregexPattern np = TregexPattern.compile("NP|NNP|NNPS|NN|NNS");
		  TregexMatcher m = np.matcher(tree);
		  while(m.find())
		  {
			  Tree npTree = m.getMatch();
			  List<Token> tokens = Utils.getTokens(tree, npTree);

			  if(!npTree.isLeaf())
			  {
				  Entity candidate = new Entity("",npTree.nodeString(),tokens);
		  
					if (entity.from() > candidate.to()) {
						if (intraSentence) {
							if ((entity.from() > start) && (entity.to() < end))
								entityList.add(candidate);
						} else
							entityList.add(candidate);
					}
			  }
		  } 
	  }
	  return entityList;
  }
  
  protected boolean entityInArgument(Entity entity, Entity argument, HashMap<String,HashMap<String,String>> map)
  {
		String word = argument.getText();
		String pattern = Env.ENTITY_REPLACE;
    	Pattern r = Pattern.compile(pattern);
    	Matcher m = r.matcher(word);
    	
    	while(m.find())
    	{
    		String needle = m.group();
    		try {
    			int entStart = Integer.parseInt(map.get(needle).get("start"));
    			int entend = Integer.parseInt(map.get(needle).get("end"));

    			int start = entity.from();
    			int end = entity.to();

    			if(start == entStart && entend == end)
    				return true;
    			
    		} catch (Exception e) {
    			System.out.println(filename);
    			System.out.println(needle);
    			System.exit(1);
    		}
    	}
    	return false;
  }
  
  @Override
  protected void readResource(String dir, String filename) {
  }

}
