package mods.pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import mods.ptb.PtbReader;
import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import mods.annotation.A3EventReader;
import mods.annotation.CompositeEvent;
import mods.annotation.Event;
import mods.annotation.Entity;
import mods.annotation.Event.ArgumentType;
import mods.annotation.Event.EventType;
import mods.annotation.A1EntityReader;

public class ExtractTarget extends FileProcessor {
	
  @Override
  public void processFile(String dir, String filename) {
    super.processFile(dir, filename);
    // generate target file
    try {
		printA4(dir, filename);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

  protected void printA4(String dir, String filename) throws IOException {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/"
          + filename
          + ".Target"));

      // get parsed sentences for co-occurrence detection
      List<String> parsed = Arrays.asList(StringUtils.split(FileUtils.readFileToString(new File(Env.DIR_SIMP+filename+".ptb")), '\n'));
      //List<String> parsed = Files.readAllLines(Paths.get(Env.DIR_SIMP+filename+".ptb"), StandardCharsets.UTF_8);
      
      // get original sentences for co-occurrence detection
      List<String> sentences = Arrays.asList(StringUtils.splitByWholeSeparator(FileUtils.readFileToString(new File(Env.DIR_PARSE+filename+".split")), "</s>"));
      List<String> filteredSens = new ArrayList<String>();
      for(String sentence : sentences)
      {
    	  if(sentence.trim().length() > 0)
    	  {
    		  sentence = sentence.replace("\n", " ");
    		  sentence = sentence.replace("<s>", "");
    		  filteredSens.add(sentence);
    	  }
    		  
      }
      sentences = filteredSens;
      //List<String> sentences = new ArrayList<String>(); 
      
      // get all the entitis in a1 file
      List<Entity> a1ts = A1EntityReader.readEntities(dir + "/" + filename + ".a1");
      // sort entities based on positions, ensure in-order replacement
      Collections.sort(a1ts);
      
      // read ENTITY-string and entity mapping
      String mapText = dir + "/" + filename + ".entmap";
      File file = new File(mapText);
      HashMap<String,HashMap<String,String>> map = new HashMap<String,HashMap<String,String>>();
      try {
      	String mapstr = FileUtils.readFileToString(file);
      	Gson gson = new Gson();
      	Type mapType = new TypeToken<HashMap<String,HashMap<String,String>>>() {}.getType();
      	map = gson.fromJson(mapstr, mapType);
//      	
//      	Type sentenceType = new TypeToken<List<String>>() {}.getType();
//      	String sentencesStr = FileUtils.readFileToString(new File(Env.DIR_PARSE+filename+".split"));
//      	sentences = gson.fromJson(sentencesStr, sentenceType);
      } catch (Exception e) {
    	  System.out.println("gson error");
    	  System.out.println(filename);
      	System.exit(1);
      }
      
      // each trigger's argument groups, no matter it's agent/theme, miRNA/gene
      // a3.*
      HashMap<Entity,List<Entity>> argGroups = new HashMap<Entity,List<Entity>>();
      
      // each trigger's argument groups, separated by agent/theme
      // a3/a3.Causal/a3.Anaphora
      HashMap<Entity,HashMap<String,List<Entity>>> argRoleGroups = new HashMap<Entity,HashMap<String,List<Entity>>>();
      
      // each valid miRNA-gene trigger's argument groups, separated by agent/theme
      // .Target
      HashMap<Entity,HashMap<String,List<Entity>>> vargRoleGroups = new HashMap<Entity,HashMap<String,List<Entity>>>();
      
      // trigger word argument type count
      HashMap<Entity,Integer> typeCount = new HashMap<Entity,Integer>();

      // get all the events in a3 files
      List<Event> a3es = new ArrayList<Event>();
      
      // get all the events in original a3 file
      List<Event> a3esOriginal = new ArrayList<Event>();   
      
      // get all the events in modifier file
      List<Event> modifiers = new ArrayList<Event>();
      
      // get all composite events
      List<CompositeEvent> a3ces = new ArrayList<CompositeEvent>();
      
      // get all composite events copy
      List<CompositeEvent> a3cces = new ArrayList<CompositeEvent>();
      
      // trigger modifier map
      HashMap<Entity,Integer> modifierMap= new HashMap<Entity,Integer>();
      
      // relation as argument map, i.e., the whole relation is an argument of another relation
      HashMap<Entity,Integer> argRelMap= new HashMap<Entity,Integer>();
      
      // trigger map, i.e., if trigger is target, bind, interact.
      HashMap<Entity,Integer> triggerMap= new HashMap<Entity,Integer>();
      
      // aggregate events by trigger words
      HashMap<Entity,List<CompositeEvent>> a3cmap= new HashMap<Entity,List<CompositeEvent>>();
      
      for (EventType type : EventType.values()) {
        File a3file = new File(dir + "/" + filename + ".a3." + type);
        if (!a3file.exists()) {
          continue;
        }
        a3es.addAll(A3EventReader.readEvents(dir + "/" + filename + ".a3." + type));
      }

      a3esOriginal.addAll(A3EventReader.readEvents(Env.DIR + "/" + filename + ".a3"));
      a3esOriginal.addAll(A3EventReader.readEvents(Env.DIR + "/" + filename + ".a3.Causal"));
      a3esOriginal.addAll(A3EventReader.readEvents(Env.DIR + "/" + filename + ".a3.Anaphora"));
      
      modifiers.addAll(A3EventReader.readEvents(Env.DIR_REF + "/" + filename + ".mod"));
      
      for(Event e : modifiers)
      {
    	  if(e.argument.getText().equalsIgnoreCase("direct") ||
    			  e.argument.getText().equalsIgnoreCase("directly") )
    	  modifierMap.put(e.trigger, 1);
    	  else if(e.argument.getText().equalsIgnoreCase("indirect") ||
    			  e.argument.getText().equalsIgnoreCase("indirectly") )
    	  modifierMap.put(e.trigger, -1);
    	  else if(e.argument.getText().equalsIgnoreCase("positive") ||
    			  e.argument.getText().equalsIgnoreCase("positively") )
    	  modifierMap.put(e.trigger, 2);
    	  else if(e.argument.getText().equalsIgnoreCase("negative") ||
    			  e.argument.getText().equalsIgnoreCase("negatively") )
    	  modifierMap.put(e.trigger, -2);
    	  else if(e.argument.getText().equalsIgnoreCase("inverse") ||
    			  e.argument.getText().equalsIgnoreCase("inversely") )
    	  modifierMap.put(e.trigger, -3);
      }
      
      // aggregate arguments for each trigger for co-occurrence
      for(Event e : a3esOriginal)
      {
    	  // save all arguments based on roles, for filtering miRNA in theme position
    	  List<Entity> agentList = new ArrayList<Entity>();
    	  List<Entity> themeList = new ArrayList<Entity>();
    	  
		  for(Entity a1entity : a1ts)
		  {
			  if(entityInArgument(a1entity,e.argument,map))
			  {
				  
				  if(e.argumentType == Event.ArgumentType.Agent && (
						  e.comment.startsWith("# Arg Vact") ||
						  e.comment.startsWith("# Arg to Vact") ||
						  e.comment.startsWith("# Arg Vpass") ||
						  e.comment.startsWith("# Vpass by Arg") ||
						  e.comment.startsWith("# Vnorm of Arg by Arg") ||
						  //e.comment.startsWith("# Arg does something by Vvbg") ||
						  e.comment.startsWith("# Arg does something through -ion")) &&
						  !(e.trigger.getText().startsWith("bind") || e.trigger.getText().startsWith("bound") ||
								  e.trigger.getText().startsWith("associat") || e.trigger.getText().startsWith("correlat") ||
								  e.trigger.getText().startsWith("interact") || e.trigger.getText().startsWith("relat")))
					  agentList.add(a1entity);
				  
				  if(e.argumentType == Event.ArgumentType.Theme && (
						  e.comment.startsWith("# Vact Arg") ||
						  e.comment.startsWith("# Vact to Arg") ||
						  e.comment.startsWith("# Vadj to Arg") ||
						  e.comment.startsWith("# Ving to Arg") ||
						  e.comment.startsWith("# Vact at Arg") ||
						  //e.comment.startsWith("# Vadj Arg") ||
						  e.comment.startsWith("# Arg Vadj") ||
						  e.comment.startsWith("# Vnorm of Arg on Arg")
						  ) && !(e.trigger.getText().startsWith("bind") || e.trigger.getText().startsWith("bound")))
					  themeList.add(a1entity);
			  }
		  }

		  HashMap<String,List<Entity>> argRoleGroup = argRoleGroups.get(e.trigger);
		  if(argRoleGroup == null)
		  {
			  argRoleGroup = new HashMap<String,List<Entity>>();
			  argRoleGroup.put("Agent", agentList);
			  argRoleGroup.put("Theme", themeList);
			  argRoleGroups.put(e.trigger, argRoleGroup);
		  }
		  else
		  {
			  argRoleGroup.get("Agent").addAll(agentList);
			  argRoleGroup.get("Theme").addAll(themeList);
		  }
      }
      
      for(Event e : a3es)
      {
    	  // save all arguments for later use for co-occurrence check
    	  List<Entity> allArgList = new ArrayList<Entity>();

		  for(Entity a1entity : a1ts)
		  {
			  if(entityInArgument(a1entity,e.argument,map))
			  {
				  allArgList.add(a1entity);
			  }
		  }
		  List<Entity> argGroup = argGroups.get(e.trigger);
		  if(argGroup == null)
			  argGroups.put(e.trigger, allArgList);
		  else
		  {
			  allArgList.addAll(argGroup);
			  argGroups.put(e.trigger, allArgList);
		  }
      }
      
      for(Event e : a3es)
      {
    	  // save valid arguments for relation extraction
    	  List<Entity> argList = new ArrayList<Entity>();

    	  CompositeEvent ce = null;
    	  
    	  int direction = direction(e.argument);

    	  boolean translation = false;
    	  
    	  // type count is used to record how many different types of arguments the trigger has 
    	  Integer typeCnt = typeCount.get(e.trigger);
    	  if(typeCnt == null)
    		  typeCnt = 1;
    	  
    	  if(e.trigger.getText().toLowerCase().startsWith("bind") || 
    			  e.trigger.getText().toLowerCase().equals("bound") || 
    			  e.trigger.getText().toLowerCase().startsWith("interact") ||
    			  e.trigger.getText().toLowerCase().startsWith("associat") ||
    			  e.trigger.getText().toLowerCase().startsWith("correlat") ||
    			  e.trigger.getText().toLowerCase().startsWith("relat"))
    	  {
    		  ArgumentType ct = null;
    		  // for these trigger words, agent and theme might be caught together, e.g., relation between miRNA and gene.
    		  // we only process situations which have single role
    		  boolean singleRole = true;
    		  
    		  for(Entity a1entity : a1ts)
    		  {
    			  if(!entityInArgument(a1entity,e.argument,map))
    				  continue;

    			  if(a1entity.type.equals("MiRNA"))
    			  {
    				  argList.add(a1entity);
    				  if(ct == ArgumentType.Theme)
    					  singleRole = false;
    				  ct = ArgumentType.Agent;
    			  }
    			  if(!validateInduce(e,a1entity,map))
    				  continue;

    			  if((validateEntity(e,a1entity,map) || validatePhrase(e,a1entity)))
    			  {
    				  if(a1entity.type.equals("Gene") || a1entity.type.equals("Family") || a1entity.type.equals("Complex"))
    				  {
    					  argList.add(a1entity);
    					  if(ct == ArgumentType.Agent)
    						  singleRole = false;
    					  ct = ArgumentType.Theme;
    				  }
    			  }
    		  }
    		  
    		  if((argList.size() > 0) && singleRole)
    		  {   
    			  if((ct == ArgumentType.Theme) && (typeCnt%3 > 0))
    				  typeCount.put(e.trigger, 3*typeCnt);
    			  if((ct == ArgumentType.Agent) && (typeCnt%2 > 0))
    				  typeCount.put(e.trigger, 2*typeCnt);

    			  ce = new CompositeEvent(filename, e.type, e.trigger, ct, argList, e.comment);
    		  }
    	  }
    	  
    	  
    	  else if(e.argumentType == Event.ArgumentType.Agent)
    	  {
    		  for(Entity a1entity : a1ts)
    		  {
    			  if(!entityInArgument(a1entity,e.argument,map))
    				  continue;
    			     			  
    			  if(e.comment.startsWith("# Target Agent Same NP"))
    			  {
    				  if(e.trigger.to() < a1entity.to() && e.comment.indexOf("-REF") != -1)
    					  continue;
    			  }
    			  
    			  if(entityInArgument(a1entity,e.argument,map) && a1entity.type.equals("MiRNA"))
    				  argList.add(a1entity);
    		  }
    		  if(argList.size() > 0)
    		  {
    			  ce = new CompositeEvent(filename, e.type, e.trigger, e.argumentType, argList, e.comment);
    			  if(typeCnt%2 > 0)
    				  typeCount.put(e.trigger, 2*typeCnt);
    		  }
    	  }

    	  else if(e.argumentType == Event.ArgumentType.Theme)
    	  {
    		  
    		  for(Entity a1entity : a1ts)
    		  {
    			  if(!entityInArgument(a1entity,e.argument,map))
    				  continue;
    			  
    			  if(!validateInduce(e,a1entity,map))
    				  continue;

    			  if(e.comment.startsWith("# Target Theme Same NP"))
    			  {
    				  if(e.trigger.to() > a1entity.to() && e.comment.indexOf("-REF") != -1)
    					  continue;
    			  }
    			  
    			  if((validateEntity(e,a1entity,map) || validatePhrase(e,a1entity)) && !a1entity.type.equals("MiRNA"))
    			  {
    				  translation = translation(e.argument);
    				  argList.add(a1entity);
    			  }
    		  }
    		  if(argList.size() > 0)
    		  {
    			  ce = new CompositeEvent(filename, e.type, e.trigger, e.argumentType, argList, e.comment);
    			  ce.translation = translation;
    			  if(typeCnt%3 > 0)
    				  typeCount.put(e.trigger, 3*typeCnt);
    		  }
    	  }

    	  
    	  if(ce != null)
    	  {
    		  ce.direction = direction;
    		  a3ces.add(ce);
    	  }
      }

      a3cces.addAll(a3ces);
      
      // to remove wrongly cascading event
      // e.g., gene1 suppression by miRNA regulates gene2
      for(CompositeEvent cce : a3cces)
      {
    	  boolean acceptFlag = true;
    	  
    	  for(CompositeEvent ce : a3ces)
    	  {
    		  if(cce.argumentList.size() == ce.argumentList.size())
    		  {
    			  boolean sameArgFlag = true;
    			  for(Entity e : cce.argumentList)
    			  {
    				  if(!ce.argumentList.contains(e))
    					  sameArgFlag = false;
    			  }
    			  if(sameArgFlag)
    			  {
    				  if((ce.comment.startsWith("# Arg Vnorm") ||
    						  ce.comment.startsWith("# Vnorm of Arg")) &&
    						  (!cce.comment.startsWith("# Arg Vnorm") &&
    	    						  !cce.comment.startsWith("# Vnorm of Arg")) &&
    						  (ce.argumentType == ArgumentType.Agent) && (typeCount.get(ce.trigger) == 6))
    					  //acceptFlag = false;
    				  {
    					  argRelMap.put(cce.trigger, 1);
    				  }
    			  }
    		  }
    	  }
    	  
    	  if(acceptFlag)
    	  {
    		  if(a3cmap.containsKey(cce.trigger))
    		  {
    			  a3cmap.get(cce.trigger).add(cce);
    		  }
    		  else
    		  {
    			  List<CompositeEvent> ceventList = new ArrayList<CompositeEvent>();
    			  ceventList.add(cce);
    			  a3cmap.put(cce.trigger, ceventList);
    		  }
    	  }
      }

      
      for (Map.Entry<Entity, List<CompositeEvent>> entry : a3cmap.entrySet()) {

    	    List<CompositeEvent> eventList = entry.getValue();
    	    Entity trigger = entry.getKey();
    	    List<String> agentList = new ArrayList<String>();
    	    List<String> themeList = new ArrayList<String>();
    	    Set<String> agentRuleList = new HashSet<String>();
    	    Set<String> themeRuleList = new HashSet<String>();
    	    
    	    List<Entity> agentEntList = new ArrayList<Entity>();
    	    List<Entity> themeEntList = new ArrayList<Entity>();
    	    
    	    String directness = "unknown";
    	   
    	    int agentDirection = 2;
    	    int themeDirection = 2;
    	    int triggerDirection = 2;

    	    String triText = trigger.getText().toLowerCase();
    	    
    	    if(triText.startsWith("target") ||
    	    		triText.startsWith("bind") ||
    	    		triText.startsWith("bound") ||
    	    		triText.startsWith("interact") ||
    	    		triText.endsWith("targeting") ||
    	    		triText.endsWith("targeted") ||
    	    		triText.endsWith("binding") ||
    	    		triText.endsWith("bound")
    	    		)
    	    	triggerMap.put(trigger, 1);

    	    if(triText.startsWith("regulat") ||
    	    		(modifierMap.get(trigger) != null && modifierMap.get(trigger) == 2))
    	    	//&& trigger.tokens.get(0).pos.startsWith("V"))
    	    {
    	    	triggerDirection = 0;
    	    }
    	    
    	    if((triText.startsWith("increas") ||
    	    		triText.startsWith("amplif") ||
    	    		triText.startsWith("enhanc") ||
    	    		triText.startsWith("promot") ||
    	    		triText.startsWith("up-regulat") ||
    	    		triText.startsWith("upregulat")) ||
    	    		(modifierMap.get(trigger) != null && modifierMap.get(trigger) == 2))
    	    	//&& trigger.tokens.get(0).pos.startsWith("V"))
    	    {
    	    	triggerDirection = 1;
    	    }

    	    if((triText.startsWith("decreas") ||
    	    		triText.startsWith("down-regulat") ||
    	    		triText.startsWith("downregulat") ||
    	    		triText.startsWith("suppress") ||
    	    		triText.startsWith("repress") ||
    	    		triText.startsWith("reduc") ||
    	    		triText.startsWith("inhibit") ||
    	    		triText.startsWith("silenc") ||
    	    		triText.startsWith("diminish") ||
    	    		triText.startsWith("block")) ||
    	    		(modifierMap.get(trigger) != null && modifierMap.get(trigger) == -2))
    	    	//&& trigger.tokens.get(0).pos.startsWith("V"))
    	    {
    	    	triggerDirection = -1;
    	    }
    	    
    	    Integer nullArg = 0;
    	    Integer translation = 0;
    	    
    	    List<CompositeEvent> eventListTypedAnaphora = new ArrayList<CompositeEvent>();
    	    Integer nonTypedAnaphora = 0;
    	    
    	    for(CompositeEvent e : eventList)
    	    {
    	    	if(e.comment.endsWith("NonTypedAnaphora"))
    	    	{
    	    		if(e.argumentType == ArgumentType.Agent)
    	    			nonTypedAnaphora |= 1;
    	    		else
    	    			nonTypedAnaphora |= 2;
    	    		if(nonTypedAnaphora == 3)
    	    			continue;
    	    	}
    	    	eventListTypedAnaphora.add(e);
    	    }
    	    eventList = eventListTypedAnaphora;
    	    
    	    for(CompositeEvent e : eventList)
    	    {
    	    	if(e.comment.startsWith("# Arg does something by Vvbg") ||
    	    			e.comment.startsWith("# Arg does something through -ion"))
    	    		nullArg = 1;
    	    	
    	    	if(e.argumentType == ArgumentType.Agent)
    	    	{
    	    		if(!e.comment.startsWith("Vnorm between Arg"))
    	    			agentRuleList.add(e.comment);
	    			
    	    		for(Entity entity : e.argumentList)
    	    		{
    	    			agentList.add(entity.getText());
    	    			if(!agentEntList.contains(entity))
    	    				agentEntList.add(entity);
    	    		}
    	    		if(agentDirection == 2 && !e.comment.contains("Anaphora"))
    	    			agentDirection = e.direction;
    	    	}
    	    	
    	    	if(e.argumentType == ArgumentType.Theme)
    	    	{
    	    		if(!e.comment.startsWith("# Vnorm between Arg"))
    	    			themeRuleList.add(e.comment);
    	    		for(Entity entity : e.argumentList)
    	    		{
    	    			themeList.add(entity.getText());
    	    			if(!themeEntList.contains(entity))
    	    				themeEntList.add(entity);
    	    		}
    	    		if(themeDirection == 2 && !e.comment.contains("Anaphora"))
    	    			themeDirection = e.direction;
    	    	}
  
    	    	if(e.translation)
    	    		translation = 1;
    	    }
    	    
	    	Integer isModified = modifierMap.get(trigger);
	    	if(isModified == null)
	    		isModified = 0;
    	    
    	    int polar = 0;
	    	int production = agentDirection * themeDirection * triggerDirection;
 	
	    	if(production < 8 && production > 0)
	    	{
	    		polar = 1;
	    	}
	    	
	    	// calculation is positive or inversion is mentioned
	    	if(production < 0 || isModified == -3)
	    		polar = -1;
    	      	    
	    	Integer isPositive = polar;
	    	
	    	Integer isTarget = triggerMap.get(trigger);
	    	if(isTarget == null)
	    		isTarget = 0;

	    	Integer isArgRel = argRelMap.get(trigger);
	    	if(isArgRel == null)
	    		isArgRel = 0;

    	    int sid = get_sentence(trigger,parsed);
    	    int co1 = 0;
    	    int co2 = 0;
    	    
    	    co1 = co1 | occurrence(sentences.get(sid-1),"3'UTR");
    	    co1 = co1 | occurrence(sentences.get(sid-1),"3'-UTR");
    	    co1 = co1 | occurrence(sentences.get(sid-1),"3' UTR");
    	    co2 = co2 | occurrence(sentences.get(sid-1),"3' untranslated region");
    	    co2 = co2 | occurrence(sentences.get(sid-1),"3'-untranslated region");
    	    co2 = co2 | occurrence(sentences.get(sid-1),"3'untranslated region");
    	    
    	    directness = judge(isTarget,isModified,isPositive,isArgRel,nullArg,translation,co1,co2);
    	    
    	    String agents = StringUtils.join(new ArrayList<String>(new HashSet<String>(agentList)), ", ");
    	    String themes = StringUtils.join(new ArrayList<String>(new HashSet<String>(themeList)), ", ");


    	    if(agents.length() == 0 || themes.length() == 0)
    	    	continue;
    	    
    	    // agent and theme can't be extracted using the same rule
    	    if((agentRuleList.size() == themeRuleList.size()) && agentRuleList.equals(themeRuleList) && agentRuleList.size() > 0)
    	    	continue;
    	     
    	    HashMap<String,List<Entity>> vargRole = new HashMap<String,List<Entity>>();
    	    vargRole.put("Agent", agentEntList);
    	    vargRole.put("Theme", themeEntList);
    	    vargRoleGroups.put(trigger, vargRole);
    	    String markedSentence = mark_sentence(sentences.get(sid-1),agentEntList,themeEntList,map);
    	    String row = filename + "\t" + agents + "\t" + themes + "\t" + directness + "\t" +markedSentence.trim()+"\t"+Integer.toString(sid-1);
    	    out.println(row);
    	    ann4relations(dir,filename,trigger,agentEntList,themeEntList,directness);
    	}
      
      out.close();
     
      co_entities(sentences,parsed,a1ts,argGroups,argRoleGroups,vargRoleGroups,map);
      
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void ann4relations(String dir, String filename, Entity trigger, List<Entity> agentList, List<Entity> themeList, String directness)
  {
	  try {
		  PrintStream out = new PrintStream(new FileOutputStream(dir + "/" + filename + ".a2",true));

		  String direct = "U";
		  if(directness.indexOf("direct") != -1)
			  direct = "D";
		  
		  for(Entity agent : agentList)
			  for(Entity theme : themeList)
			  {
				  if(theme.type == "Family")
					  continue;
				  String relline = Integer.toString(trigger.from()) + " " + Integer.toString(trigger.to()) + 
						  "\t" + Integer.toString(agent.from()) + " " + Integer.toString(agent.to()) +
						  "\t" + Integer.toString(theme.from()) + " " + Integer.toString(theme.to()) + "\t"+direct+"\tM2G\tR";
				  out.println(relline);
			  }
		  out.close();
	  }
	  catch(Exception e) {

	  }
	  return;
  }
  
  private void ann4corelations(String dir, String filename, Entity agent, Entity theme, String directness)
  {
	  try {
		  PrintStream out = new PrintStream(new FileOutputStream(dir + "/" + filename + ".a2",true));

		  String direct = "U";
		  if(directness.indexOf("direct") != -1)
			  direct = "D";

		  if(theme.type == "Family")
		  { 
			  out.close();
			  return;
		  }
		  
		  String relline = Integer.toString(agent.from()) + " " + Integer.toString(agent.to()) +
				  "\t" + Integer.toString(theme.from()) + " " + Integer.toString(theme.to()) + "\t"+direct+"\tM2G\tC";
		  out.println(relline);

		  out.close();
	  }
	  catch(Exception e) {

	  }
	  return;
  }
  
  private String mark_sentence(String sentence, List<Entity> agentEntList,
		List<Entity> themeEntList, HashMap<String, HashMap<String, String>> map) {
	  
	 String pattern = Env.ENTITY_REPLACE;
	 Pattern r = Pattern.compile(pattern);
	 Matcher m = r.matcher(sentence);
	 
	 while(m.find())
	 {
		 String needle = m.group();
		 HashMap<String, String> entity = map.get(needle);
		 int start = Integer.parseInt(entity.get("start"));
		 int end = Integer.parseInt(entity.get("end"));
		 String text = entity.get("text");
		 boolean replaced = false;
		 
		 for(Entity e : agentEntList)
		 {
			 if(e.from() == start && e.to() == end)
			 {
				 sentence = sentence.replaceFirst(needle, "<mir>"+text+"</mir>");
				 replaced = true;
				 break;
			 }
		 }
		 
		 if(replaced)
			 continue;
		 
		 for(Entity e : themeEntList)
		 {
			 if(e.from() == start && e.to() == end)
			 {
				 if(e.type.equals("Gene"))
					 sentence = sentence.replaceFirst(needle, "<gene>"+text+"</gene>");
				 else if(e.type.equals("Family"))
					 sentence = sentence.replaceFirst(needle, "<family>"+text+"</family>");
				 else if(e.type.equals("Complex"))
					 sentence = sentence.replaceFirst(needle, "<complex>"+text+"</complex>");
				 replaced = true;
				 break;
			 }
		 }
		 
		 if(replaced)
			 continue;
		 
		 sentence = sentence.replaceFirst(needle, text);
		 
	 }
	return sentence;
}

// find co-occurrence entities not in the argument groups
  private void co_entities(List<String> sentences,List<String> parsed,
		  List<Entity> a1ts,HashMap<Entity,List<Entity>> argGroups,
		  HashMap<Entity,HashMap<String,List<Entity>>> argRoleGroups,
		  HashMap<Entity,HashMap<String,List<Entity>>> vargRoleGroups,
		  HashMap<String,HashMap<String,String>> map)
  {
	  
      if(sentences.size() != parsed.size())
      {
    	  System.out.println(filename);
    	  System.out.println("unmatched number of sentences and parses");
    	  return;
      }
      
      String abs = dir + "/" + filename + ".ori";
      String absText = "";
      File file = new File(abs);
      try {
      	absText = FileUtils.readFileToString(file);
      } catch (Exception e) {
    	  System.out.println("ExtractTarget.java - reading abs text file");
      }
          
      // reverse map for map, for later regular expression usage
      HashMap<Entity,String> reverseMap = new HashMap<Entity,String>();
      for(Map.Entry<String, HashMap<String,String>> entry : map.entrySet())
      {
    	  String idx = entry.getKey();
    	  int start = Integer.parseInt(entry.getValue().get("start"));
    	  int end = Integer.parseInt(entry.getValue().get("end"));
    	  for(Entity e : a1ts)
    	  {
    		  if(e.from() == start && e.to() == end)
    		  {
    			  reverseMap.put(e, idx);
    			  break;
    		  }
    	  }
      }
      
      // rule-based argument strings
      HashMap<Entity,List<String>> vargGroups = new HashMap<Entity,List<String>>();
      for(Map.Entry<Entity, HashMap<String,List<Entity>>> entry : vargRoleGroups.entrySet())
      {
    	  List<Entity> aEntLst = entry.getValue().get("Agent");
    	  List<Entity> tEntLst = entry.getValue().get("Theme");
    	  Entity trigger = entry.getKey();
    	  if(vargGroups.get(trigger) == null)
    		  vargGroups.put(trigger, new ArrayList<String>());
    	  
    	  for(Entity e : aEntLst)
    	  {
    		  vargGroups.get(trigger).add(e.getText().toLowerCase());
    	  }
    	  for(Entity e : tEntLst)
    	  {
    		  vargGroups.get(trigger).add(e.getText().toLowerCase());
    	  }
      }
      
	  try {
	      PrintStream out = new PrintStream(new FileOutputStream(Env.DIR
	              + "/"
	              + filename
	              + ".Co"));
	      
		  int sid = 0;
		  
		  String pattern = Env.ENTITY_REPLACE;
		  Pattern r = Pattern.compile(pattern);
		  
		  
		  String pattern1 = "(,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))*[\\-\\s](null|insufficiency|sensitivity|sensitive|negative|positive|deficient|resistant|effect|resistance|status)";
		  String pattern2 = "(response|sensitivity|negative|positive|deficiency|resistance|effect|insufficiency|status|null) (of|to) (,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))*";
		  String pattern3 = "(,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))* (pathway|signaling|therapy|network)";
		  String pattern4 = "(,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))* (protein )?(expression|production|level)s?";
		  String pattern5 = "(protein )?(expression|production|level)s? of (,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))*";
		  String pattern6 = "(^|\\s)(antisense|anti|as|amo)( |-)";
		  String pattern7 = "(,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))*-(induced|dependent|mediated|related)";
		  String pattern8 = "translation of (,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))*";
		  String pattern9 = "(,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))*translation";
		  
		  // 1: positive/deficient/resistant/effect
		  // 2: pathway/signaling
		  // 4: dicer
		  // 8: expression/production/level
		  // 16: anti/as-miRNA
		  // other features: opposite direction, conjunction, in-between trigger
		  // are not tied to a single entity
	      HashMap<Entity,Integer> entFeature = new HashMap<Entity,Integer>();
		  
	      // record entity in-sentence position for highlight
	      HashMap<Entity,List<Integer>> entPosition = new HashMap<Entity,List<Integer>>();

		  for(String s : sentences)
		  {
			  List<Entity> coMiRNA = new ArrayList<Entity>();
			  List<Entity> coGene = new ArrayList<Entity>();
			  s = s.replace("<s>", "").replace("</s>", "").trim();
			  Matcher m = r.matcher(s);
			  
			  String sentence = sentences.get(sid);
			  sentence = sentence.replace("<s>", "").replace("</s>", "").trim();
			 
			  String parse = parsed.get(sid);

			  while(m.find())
			  {
				  String needle = m.group();
				  Integer feature = 0;

				  // feature extraction for co-occurrence entities
				  String pattern11 = needle + pattern1;
				  String pattern22 = pattern2 + needle;
				  String pattern33 = needle + pattern3;
				  String pattern44 = needle + pattern4;
				  String pattern55 = pattern5 + needle;
				  String pattern66 = pattern6 + needle;
				  String pattern77 = needle + pattern7;
				  String pattern88 = pattern8 + needle;
				  String pattern99 = needle + pattern9;
				  
				  Pattern r1 = Pattern.compile(pattern11,Pattern.CASE_INSENSITIVE);
				  Pattern r2 = Pattern.compile(pattern22,Pattern.CASE_INSENSITIVE);
				  Pattern r3 = Pattern.compile(pattern33,Pattern.CASE_INSENSITIVE);
				  Pattern r4 = Pattern.compile(pattern44,Pattern.CASE_INSENSITIVE);
				  Pattern r5 = Pattern.compile(pattern55,Pattern.CASE_INSENSITIVE);
				  Pattern r6 = Pattern.compile(pattern66,Pattern.CASE_INSENSITIVE);
				  Pattern r7 = Pattern.compile(pattern77,Pattern.CASE_INSENSITIVE);
				  Pattern r8 = Pattern.compile(pattern88,Pattern.CASE_INSENSITIVE);
				  Pattern r9 = Pattern.compile(pattern99,Pattern.CASE_INSENSITIVE);
				  
				  Matcher m1 = r1.matcher(sentence);
				  Matcher m2 = r2.matcher(sentence);
				  Matcher m3 = r3.matcher(sentence);
				  Matcher m4 = r4.matcher(sentence);
				  Matcher m5 = r5.matcher(sentence);
				  Matcher m6 = r6.matcher(sentence);
				  Matcher m7 = r7.matcher(sentence);
				  Matcher m8 = r8.matcher(sentence);
				  Matcher m9 = r9.matcher(sentence);
				  
				  if(m1.find() || m2.find())
					  feature = feature | 1;
				  if(m3.find())
					  feature = feature | 2;
				  if(m4.find() || m5.find())
					  feature = feature | 8;
				  if(m6.find())
					  feature = feature | 16;      
				  if(m7.find())
					  feature = feature | 256;
				  
				  // translation flag
				  if(m8.find() || m9.find())
					  feature = feature | 1024;

				  try {
					  int start = Integer.parseInt(map.get(needle).get("start"));
					  int end = Integer.parseInt(map.get(needle).get("end"));
					  				  
					  for(Entity e : a1ts)
					  {
						  if(start == e.from() && end == e.to())
						  {
							  int mstart = m.start();
							  int mend = m.start() + e.getText().length();

							  ArrayList<Integer> entPos = new ArrayList<Integer>();
							  entPos.add(mstart);
							  entPos.add(mend);
							  entPosition.put(e, entPos);
							  
							  s = s.replaceFirst(needle, e.getText());
							  m = r.matcher(s);
							  
							  if(e.type.equals("MiRNA"))
							  {
								  if(!coMiRNA.contains(e))
									  coMiRNA.add(e);
							  }
							  else if(!e.type.equals("Family"))
							  {
								  if(e.getText().equalsIgnoreCase("dicer"))
									  feature = feature | 4;
								  if(!coGene.contains(e))
									  coGene.add(e);
							  }
							  entFeature.put(e, feature);
							  break;
						  }
					  }
				  } catch (Exception e) {
					  System.out.println(filename);
					  System.out.println(needle);
					  //System.exit(1);
				  }
			  }

			  
			  // loop through all miRNA-gene pairs
			  for(Entity mir : coMiRNA)
			  {
				  for(Entity gene : coGene)
				  {
					  boolean notRule = true;
					  int mirRole = 0;
					  int geneRole = 0;
					  boolean geneInOther = false;
					  boolean mirInOther = false;
					  
					  for(Map.Entry<Entity, List<Entity>> entry : argGroups.entrySet())
					  {
						  List<Entity> args = entry.getValue();
//						  String geneStem = gene.getText().toLowerCase();
//						  geneStem = geneStem.replaceAll("(gene|protein)$","");
//						  geneStem = geneStem.trim();
						  
						  // skip those already extracted by rules
						  if(args.contains(mir) && args.contains(gene))
						  {
							  notRule = false;
							  break;
						  }
					  }
					  
					  for(Map.Entry<Entity, List<String>> entry : vargGroups.entrySet())
					  {
						  List<String> vargs = entry.getValue();
						  if(vargs.contains(gene.getText().toLowerCase()) && vargs.contains(mir.getText().toLowerCase()))
						  {
							  notRule = false;
							  break;
						  }
					  }
					  
					  for(Map.Entry<Entity, HashMap<String,List<Entity>>> entry : argRoleGroups.entrySet())
					  {
						  HashMap<String,List<Entity>> argRoles = entry.getValue();
						  if(argRoles.get("Theme").contains(mir))
							  mirRole = mirRole | 1;
						  
						  if(argRoles.get("Agent").contains(mir))
							  mirRole = mirRole | 2;
						  
						  if(argRoles.get("Agent").contains(gene))
							  geneRole = geneRole | 1;
						  
						  if(argRoles.get("Theme").contains(gene))
							  geneRole = geneRole | 2;
					  }
						  
					  // if miR or gene in other relations, save their theme/agent for later regular expression use
					  List<Entity> knownThemes = new ArrayList<Entity>();
					  List<Entity> knownAgents = new ArrayList<Entity>();
					  
					  for(Map.Entry<Entity, HashMap<String,List<Entity>>> entry : vargRoleGroups.entrySet())
					  {
						  HashMap<String,List<Entity>> vargRoles = entry.getValue();
						  
						  if(vargRoles.get("Agent").contains(mir))
						  {
							  mirInOther = true;
							  knownThemes.addAll(vargRoles.get("Theme"));
						  }
						  
						  if(vargRoles.get("Theme").contains(gene))
						  {
							  geneInOther = true;
							  knownAgents.addAll(vargRoles.get("Agent"));
						  }
						  
						  if(vargRoles.get("Theme").contains(mir))
							  mirRole = mirRole | 1;
						  
						  if(vargRoles.get("Agent").contains(mir))
							  mirRole = mirRole | 2;
						  
						  if(vargRoles.get("Agent").contains(gene))
							  geneRole = geneRole | 1;
						  
						  if(vargRoles.get("Theme").contains(gene))
							  geneRole = geneRole | 2;
					  }
					  
					  boolean geneInConj = in_conjunction(gene,knownThemes,reverseMap,sentence,parse);
					  boolean mirInConj = in_conjunction(mir,knownAgents,reverseMap,sentence,parse);
					  
					  if(notRule)
					  {
						  int relFeature = 0;
						  int midStart = mir.from() > gene.from() ? gene.to() : mir.to();
						  int midEnd = mir.from() > gene.from() ? mir.from() : gene.from();
						  int order = in_between_order(midStart,midEnd,a1ts);
						  
						  if((order == 1) && mir.from() > gene.from())
							  relFeature = relFeature | 2;
						  if((order == 2) && mir.from() < gene.from())
							  relFeature = relFeature | 2;
						  
						  String middle = absText.substring(midStart,midEnd);
						  if(has_relation_term(middle))
							  relFeature = relFeature | 1;
						  
						  if(has_which(middle))
							  relFeature = relFeature | 4;
						  
						  int miRFeature = entFeature.get(mir);
						  int geneFeature = entFeature.get(gene);			  
						  			  
						  if((mirRole & 1) > 0 && (mirRole & 2) == 0)
							  miRFeature = miRFeature | 32;
						  if((geneRole & 1) > 0 && (geneRole & 2) == 0)
							  geneFeature = geneFeature | 32;
						  
						  if(mirInOther)
							  miRFeature = miRFeature | 64;
						  if(geneInOther)
							  geneFeature = geneFeature | 64;
						  
						  if(mirInConj)
							  miRFeature = miRFeature | 128;
						  if(geneInConj)
							  geneFeature = geneFeature | 128;
						  
						  String finalFeature = judge_co(relFeature,miRFeature,geneFeature);
	
						  if(middle.length() > 0)
						  {
							  String agents = mir.getText();
							  String themes = gene.getText();
							  int mirStart = entPosition.get(mir).get(0);
							  int mirEnd = entPosition.get(mir).get(1);
							  int geneStart = entPosition.get(gene).get(0);
							  int geneEnd = entPosition.get(gene).get(1);
							  
							  String sentenceMarked = s;
							  
							  if(mirStart > geneStart)
								 sentenceMarked = s.substring(0,geneStart)+ 
								  	"<gene>"+s.substring(geneStart,geneEnd)+"</gene>"+ 
								  	s.substring(geneEnd,mirStart)+ 
								  	"<mir>"+s.substring(mirStart,mirEnd)+"</mir>"+ 
								  	s.substring(mirEnd);
							  else
								  sentenceMarked = s.substring(0,mirStart)+
								  "<mir>"+s.substring(mirStart,mirEnd)+"</mir>"+
								  s.substring(mirEnd,geneStart)+
								  "<gene>"+s.substring(geneStart,geneEnd)+"</gene>"+
								  s.substring(geneEnd);
							  
							  // directness detection for positive co-occurrence
							  String directness = "unknown";
							  if(finalFeature.startsWith("yes"))
							  {
								  int direction = direction_term(middle);
								  if(direction == 1 || (geneFeature & 1024) > 0)
									  directness = "direct";
								  if(direction == -1)
									  directness = "indirect";
							  }
							  
							  String row = filename + "\t" + agents + "\t" + themes + "\t" + directness + "\t" +finalFeature + "\t" + sentenceMarked + "\t" + Integer.toString(sid);
							  if(finalFeature.startsWith("yes"))
								  ann4corelations(dir,filename,mir,gene,directness);
							  out.println(row);
						  }
					  }
				  }
			  }
			  
			  sid++;
		  }
		  out.close();
	  }
	  catch (FileNotFoundException e) {
	      e.printStackTrace();
	  }
  }
  
private boolean has_which(String text) {
	int index = text.toLowerCase().indexOf("which");
	if(index > -1)
	{
		String middle = text.substring(index);
		String pattern = "[^\\-](restor|rescu|target|regulat|up-regulat|down-regulat|suppress|repress|increas|decreas|inhibit|block|reduc|bind|bound|interact|dampen|relationship to|alter)(ed|ing|s|e|\\s)";
		Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(middle);
		if(m.find())
			return true;
	}
	return false;
}

private boolean in_conjunction(Entity entity, List<Entity> knownEntities,
		HashMap<Entity, String> reverseMap, String sentence, String parse) {
	
	  Tree tree = Tree.valueOf(parse);
	  
	  String conjunction = "(,|\\s|/|and|or|\\)|\\(|(N[0-9]+ENTITY))*";
	  String treePattern = "NP << /^E1_/ $+ (/,|CC/ $+ (NP << /^E2_/))";
	  
	  for(Entity ke : knownEntities)
	  {
		  String idx1 = reverseMap.get(ke);
		  String idx2 = reverseMap.get(entity);
	  
		  String conjPattern = "";
		  if(ke.from() < entity.from())
		  {
			  conjPattern = idx1 + conjunction + idx2;
			  treePattern = treePattern.replace("E1", idx1);
			  treePattern = treePattern.replace("E2", idx2);
		  }
		  else
		  {
			  conjPattern = idx2 + conjunction + idx1;
			  treePattern = treePattern.replace("E2", idx1);
			  treePattern = treePattern.replace("E1", idx2);
		  }
		  
		  Pattern mp = Pattern.compile(conjPattern,Pattern.CASE_INSENSITIVE);
		  Matcher mm = mp.matcher(sentence);
		  
		  if(mm.find())
			 return true;

		  TregexPattern tp = TregexPattern.compile(treePattern);
		  TregexMatcher tm = tp.matcher(tree);
		  if(tm.find())
			  return true;
	  }
	  
	return false;
}

private int direction_term(String middle) {
	String lower = middle.toLowerCase();
	if(lower.contains("indirect")||lower.contains("indirectly"))
		return -1;
	if(lower.contains("target")||lower.contains("direct")||lower.contains("directly")||
			lower.contains("3'UTR")||lower.contains("3' untranslated region")||lower.contains("3'untranslated region"))
		return 1;
	return 0;
}

private String judge_co(int between, int mirFeature, int geneFeature)
  {
	  String judge = "unknown";
	  String betweenStr = "";
	  String miRNAFeatureStr = "";
	  String geneFeatureStr = "";
	  
	  if((between & 1) > 0)
	  {
		  betweenStr += "trigger in between,";
		  judge = "yes";
	  }
	  if((geneFeature & 8) > 0)
	  {
		  geneFeatureStr += "gene expression,";
		  //judge = "yes";
	  }
	  if((mirFeature & 8) > 0)
	  {
		  miRNAFeatureStr += "miRNA expression,";
		  //judge = "yes";
	  }
	  if(((mirFeature & 64) > 0) && ((geneFeature & 128) > 0))
	  {
		  geneFeatureStr += "gene with known themes";
		  miRNAFeatureStr += "miRNA in other rel,";
		  judge = "yes";
	  }
	  if(((geneFeature & 64)) > 0 && ((mirFeature & 128) > 0))
	  {
		  miRNAFeatureStr += "mir with known agents";
		  geneFeatureStr += "gene in other rel,";
		  judge = "yes";
	  }
	  if((between & 2) > 0)
	  {
		  betweenStr += "reverse order in between,";
		  judge = "no";
	  }
	  if((mirFeature & 1) > 0)
	  {
		  miRNAFeatureStr += "miRNA with positive/resistance/effect/etc,";
		  judge = "no";
	  }
	  if((mirFeature & 2) > 0)
	  {
		  miRNAFeatureStr += "miRNA with signaling/pathway,";
	  	  judge = "no";
	  }
	  if((mirFeature & 16) > 0)
	  {
		  miRNAFeatureStr += "miRNA with anti/as,";
	  	  judge = "no";
	  }
	  if((mirFeature & 32) > 0)
	  {
		  miRNAFeatureStr += "miRNA only as theme,";
		  judge = "no";
	  }

	  if((geneFeature & 1) > 0)
	  {
		  geneFeatureStr += "gene with positive/resistance/effect/etc,";
		  judge = "no";
	  }
	  if((geneFeature & 2) > 0)
	  {
		  geneFeatureStr += "gene with signaling/pathway,";						  
		  judge = "no";
	  }
	  if((geneFeature & 4) > 0)
	  {
		  geneFeatureStr += "gene is dicer,";
		  judge = "no";
	  }
	  if((geneFeature & 32) > 0)
	  {
		  geneFeatureStr += "gene only as agent,";
		  judge = "no";
	  }
	  if((mirFeature & 64) > 0 && (geneFeature & 64) > 0)
	  {
		  miRNAFeatureStr += "miRNA in other rel,";
		  geneFeatureStr += "gene in other rel,";
		  judge = "no";
	  }
	  
	  // (between & 4) to check if it's a "which" case
	  // mirna regulates gene1, which in turn, regulates gene2.
	  if(((mirFeature & 64) > 0) && ((geneFeature & 128) == 0) && ((between & 4) == 0))
	  {
		  geneFeatureStr += "gene not with known themes,";
		  miRNAFeatureStr += "miRNA in other rel,";
		  judge = "no";
	  }
	  if(((geneFeature & 64)) > 0 && ((mirFeature & 128) == 0) && ((between & 4) == 0))
	  {
		  miRNAFeatureStr += "mir not with known agents,";
		  geneFeatureStr += "gene in other rel,";
		  judge = "no";
	  }
	  if((mirFeature & 256) > 0)
	  {
		  miRNAFeatureStr += "miRNA-induced,";
		  judge = "no";
	  }
	  if((geneFeature & 256) > 0)
	  {
		  geneFeatureStr += "gene-induced,";
		  judge = "no";
	  }
	  
	  betweenStr = StringUtils.strip(betweenStr,",");
	  miRNAFeatureStr = StringUtils.strip(miRNAFeatureStr,",");
	  geneFeatureStr = StringUtils.strip(geneFeatureStr,",");
	  String finalFeature = judge+"\t"+betweenStr + "|" + miRNAFeatureStr + "|" + geneFeatureStr;
	  
	  return finalFeature;
  }
  
  private int in_between_order(int start, int end, List<Entity> alts)
  {
	  String order = "";
	  int orderMap = 0;
	  for(Entity e : alts)
	  {
		  if((e.from() > start) && (e.to() < end))
		  {
			  if(e.type.equals("MiRNA"))
				  order += "R";
			  else
				  order += "G";
		  }
	  }
	  if((order.length()>1) && order.contains("RG"))
		  orderMap = orderMap | 1;
	  if((order.length()>1) && order.contains("GR"))
		  orderMap = orderMap | 2;
	  return orderMap;
  }
  
  private boolean has_relation_term(String text)
  {
	  String pattern = "[^\\-](modulat|restor|rescu|target|regulat|up-regulat|down-regulat|suppress|repress|increas|decreas|inhibit|block|reduc|bind|bound|interact|dampen|relationship to|alter)(ed|ing|s|e|\\s)";
	  Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
	  Matcher m = p.matcher(text);
	  if(m.find())
		  return true;
	  return false;
  }
  
  private int occurrence(String sentence,String text)
  {
	  if(StringUtils.containsIgnoreCase(sentence, text))
		  return 1;
	  return 0;
  }
  
  private int get_sentence(Entity trigger, List<String> parsed) {
	
	  String pos = "_"+Integer.toString(trigger.from())+"_"+Integer.toString(trigger.to());
	  int index = 1;
	  for(String p : parsed)
	  {
		  if(p.contains(pos))
			  return index;
		  index++;
	  }
	return -1;
}

protected String judge(Integer isTarget,Integer isModified,Integer isPositive, Integer isArgRel,
		Integer nullArg, Integer translation,Integer co1,Integer co2)
  {
	  String features = "\t"+isTarget.toString()+"\t"+
			 isModified.toString()+"\t"+	
			  isPositive.toString()+"\t"+
			 isArgRel.toString()+"\t"+
			  nullArg.toString()+"\t"+
			  translation.toString()+"\t"+
			  co1.toString()+"\t"+
			  co2.toString();
	  
	  if(isTarget == 1)
		  return "direct"+features;
	  if(isModified == 1)
		  return "direct"+features;
	  if(isModified == -1)
		  return "indirect"+features;
	  if(isPositive == 1)
		  return "indirect"+features;
	  if(isArgRel == 1)
		  return "indirect"+features;
	  if(nullArg == 1)
		  return "direct"+features;
	  if(translation == 1)
		  return "weak direct"+features;
	  if(co1 == 1)
		  return "weak direct"+features;
	  if(co2 == 1)
		  return "weak direct"+features;
	  return "unknown"+features;
  }
  
  protected int direction(Entity entity)
  {
	  String entText = entity.getText();	  
	  
	  /*
	  String pattern3 = "(antagonism|inhibition|suppression|decrease|repression)s? of .*?"+Pattern.quote(entText);
	  String pattern4 = Pattern.quote(entText) + "((?!of).*?)(antagonism|inhibition|suppression|decrease|repression)s?";
	  
	  String pattern5 = "(increase|increased (expression|production|translation))s? of .*?"+ Pattern.quote(entText);
	  String pattern6 = Pattern.quote(entText) + "((?!of).*?)(increase|increased (expression|production|translation))s?";

	  String pattern1 = "(activity|mRNA|expression|production|level|induction|translation|protein|.*?UTR)s? of .*?"+ Pattern.quote(entText);
	  String pattern2 = Pattern.quote(entText) + "((?!of).*?)(mRNA|expression|production|level|induction|translation|protein|activity|.*?UTR)s?";
	 	  */ 
	  
	  String pattern3 = "(low|reduction|underexpress|loss|negative|silencing|knockdown|antagonism|inhibition|inhibiting|suppression|decrease|repression|down-regulation|downregulation)s?";
	  String pattern5 = "(low|addition|restoration|over-expression|overexpression|increase|increased expression|increased production|increased translation|up-regulation|upregulation)s?";
	  String pattern6 = "(antisense|anti|as)-N[0-9]+ENTITY";
	  String pattern7 = "N[0-9]+ENTITY antagonism";
	  
	  Pattern p3 = Pattern.compile(pattern3,Pattern.CASE_INSENSITIVE);
	  Pattern p5 = Pattern.compile(pattern5,Pattern.CASE_INSENSITIVE);
	  Pattern p6 = Pattern.compile(pattern6,Pattern.CASE_INSENSITIVE);
	  Pattern p7 = Pattern.compile(pattern7,Pattern.CASE_INSENSITIVE);
	  
	  int anti = 1;
	  
	  Matcher m = p6.matcher(entText);
	  if(m.find())
	  {
		  anti = -1;
	  }
	  
	  m = p7.matcher(entText);
	  if(m.find())
		  anti = -1;
	  
	  m = p3.matcher(entText);
	  if(m.find())
	  {
		  return -1*anti;
	  }
	  
	  m = p5.matcher(entText);
	  if(m.find())
		  return 1*anti;
	  
	  if(anti == -1)
		  return -1;
	  return 2;
  }
  
  protected boolean translation(Entity entity)
  {
	  String entText = entity.getText();	  
  
	  String pattern5 = "(translation)s?";

	  Pattern p5 = Pattern.compile(pattern5,Pattern.CASE_INSENSITIVE);

	  
	  Matcher m = p5.matcher(entText);
	  if(m.find())
		  return true;
	  
	  return false;
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
    			return false;
    		}
    	}
    	return false;
  }
  
  protected boolean validateInduce(Event event, Entity entity, HashMap<String,HashMap<String,String>> map)
  {
	  // if gene-induced, false
	  // if more than 1 mentions and only 1 gene-induced, true (rare)
	  Entity argument = event.argument;
	  String argText = argument.getRepText(entity,map);

	  String entText = entity.getText();
	  /*
	  Pattern pSelf = Pattern.compile(Pattern.quote(entText),Pattern.CASE_INSENSITIVE);
	  Matcher m = pSelf.matcher(argText);
	  
	  int countSelf = 0;
	  while(m.find())
		  countSelf++;
	  
	  String pattern = Pattern.quote(entText) + "-(induced|dependent|related|mediated|producing)";
	  Pattern p = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
	  m = p.matcher(argText);
	  
	  if(m.find())
		  return false;
	  */
	  String pattern1 = Pattern.quote(entText) + "[^\\s]*?-(induced|dependent|related|mediated|producing)";
	  Pattern p1 = Pattern.compile(pattern1,Pattern.CASE_INSENSITIVE);
	  Matcher m1 = p1.matcher(argText);
	  
	  String pattern2 = Pattern.quote(entText) + ".*?[\\s\\-](signaling|pathway|network|therapy)";
	  Pattern p2 = Pattern.compile(pattern2,Pattern.CASE_INSENSITIVE);
	  Matcher m2 = p2.matcher(argText);
	   /*
	  int countErr = 0;
	  while(m1.find())
		  countErr++;
	  while(m2.find())
		  countErr++;
	  
	  if(countSelf == countErr)
		  return false;
	  */
	  if(m1.find() || m2.find())
		  return false;
	  return true;
  }
  
  protected boolean validateEntity(Event event, Entity entity, HashMap<String,HashMap<String,String>> map)
  {
	  // whole NP is a gene
	  
	  if(entity.type.equals("MiRNA"))
		  return false;
	  
	  Entity argument = event.argument;
	  
	  // expression/production/level or all are genes
	  String argText = argument.getRepText(entity,map);
	  String entText = entity.getText();
	  
	  String pattern1 = "(cell|release|activity|mRNA|expression|production|level|induction|translation|protein|.*?UTR)s? of .*?"+ Pattern.quote(entText);
	  String pattern2 = Pattern.quote(entText) + "((?!of).*?)(cell|release|mRNA|expression|production|level|induction|translation|protein|activity|.*?UTR)s?";
	  
	  // this one has problem, doesn't match the whole string
	  String pattern3 = Pattern.quote(entText) + "(N[0-9]+ENTITY|[^a-zA-Z0-9]|and|or)*?($|,|\\)|\\s|\\)|-)";
	  
	  String pattern4 = "(response|promoter|acetylation|phosphorylation|ubiquitination|neddylation|sumoylation|glycosylation)s? (of|to) .*?"+ Pattern.quote(entText);
	  String pattern5 = Pattern.quote(entText) + "((?!of).*?)(response|promoter|phosphorylation|acetylation|ubiquitination|neddylation|sumoylation|glycosylation)s?";
	  
	  Pattern p1 = Pattern.compile(pattern1,Pattern.CASE_INSENSITIVE);
	  Pattern p2 = Pattern.compile(pattern2,Pattern.CASE_INSENSITIVE);
	  Pattern p3 = Pattern.compile(pattern3,Pattern.CASE_INSENSITIVE);
	  Pattern p4 = Pattern.compile(pattern4,Pattern.CASE_INSENSITIVE);
	  Pattern p5 = Pattern.compile(pattern5,Pattern.CASE_INSENSITIVE);
	  
	  
	  Matcher m = p4.matcher(argText);
	  if(m.find())
		  return false;
	  
	  m = p5.matcher(argText);
	  if(m.find())
		  return false;
	  
	  m = p1.matcher(argText);
	  if(m.find())
	  {
		  return true;
	  }
	  
	  m = p2.matcher(argText);
	  if(m.find())
	  {
		  return true;
	  }
	  
	  m = p3.matcher(argText);

	  if(m.find())
	  {

		  return true;
	  }

	  return false;
  }
  
  static String fterm = "([^abehiou]ase$|[ -]globulin$|[ -]tubulin$|[ -]inter-?feron$|[ -]lectin$|[ -]galectin$|globin$|tinin$|matin$|ietin$|tropin$|zyme$|kine$|leukin$|nogen$|protein$|[ -]factor$|[ -]kinase$|[ -]receptor$|[ -]enzyme$|[ -]gene$|[ -]hormone$|[ -]protease$|[ -]permease$|[ -]nuclease$|[ -]oncogene$|[ -]oncogene?$|[ -]binder$)";
  
  protected boolean validatePhrase(Event event, Entity entity)
  {

	  Entity argument = event.argument;
	  String argText = argument.getRepText(entity);

	  // if is not F-term, false
	  Pattern pFterm = Pattern.compile(fterm,Pattern.CASE_INSENSITIVE);
	  Matcher m = pFterm.matcher(argText);
	  
	  if(!m.find())
		  return false;
	  
	  // if contained, true
	  if(argument.range().containsRange(entity.range()))
	  {
		  return true;
	  }

	  return false;
  }

  @Override
  protected void readResource(String dir, String filename) {
  }

}
