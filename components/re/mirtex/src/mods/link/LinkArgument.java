package mods.link;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import mods.annotation.A1EntityReader;
import mods.annotation.A3EventReader;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Event.EventType;

public class LinkArgument extends FileProcessor {

  /**
   * @param args
   */
  public static void main(String[] args) {
    LinkArgument p = new LinkArgument(true, true, false);

    if (args.length == 0) {
      args = new String[] { "PMID-7542591" };
      p.debug = true;
    }

    if (args.length == 1) {
      System.err.println("one file: " + args[0] + "!");
      p.processFile(Env.DIR, args[0]);
    } else {
      System.err.println("no file!");
    }
  }

  protected String       text;

  protected List<Entity> a1ts;
  protected List<Event>  a3es;
  protected List<Event>  refes;
  protected List<Event>  corefes;

  protected List<Entity> a7ts;
  protected List<Event>  a7es;

  protected Event        currentEvent;

  protected boolean      linkRef;
  protected boolean      linkCo;
  protected boolean      linkCoGold;
  protected HashMap<String,HashMap<String,String>> map;
  
  public LinkArgument(boolean linkRef, boolean linkCo, boolean linkCoGold) {
    this.linkRef = linkRef;
    this.linkCo = linkCo;
    this.linkCoGold = linkCoGold;
  }

  @Override
  protected void readResource(String dir, String filename) {
    // read text
    text = Utils.readText(dir + "/" + filename + ".txt");
    // a1
    a1ts = A1EntityReader.readEntities(dir + "/" + filename + ".a1");
    // a3
    try {
    	a3es = A3EventReader.readEvents(dir + "/" + filename + ".a3");
    }
    catch(Exception e) {
    	System.out.println(filename);
    }

    // ref
    refes = new ArrayList<Event>();
    if (linkRef) {
      String reffilename = Env.DIR_REF + filename + ".ref";
      refes = A3EventReader.readEvents(reffilename);
      //System.out.println(refes);
    }

    // co
    corefes = new ArrayList<Event>();
    if (linkCo) {
      // BioNex
      String bioNexfilename = dir + "/bionex/" + filename + ".co";
      List<Event> bionexEvents = A3EventReader.readEvents(bioNexfilename);
      corefes.addAll(bionexEvents);
    }

    if (linkCoGold) {
      // bionlp-co
      String cofilename = dir + "/bionlp-co/" + filename + ".a3";
      List<Event> tmp = A3EventReader.readEvents(cofilename);
      corefes.addAll(tmp);
    }

    // // part-whole
    // bioNexfilename = dir + "/bionex/" + filename + ".part";
    // bionexEvents = A3EventReader.readEvents(bioNexfilename);
    // refes.addAll(bionexEvents);
    //
    // // member-collection
    // bioNexfilename = dir + "/bionex/" + filename + ".part";
    // bionexEvents = A3EventReader.readEvents(bioNexfilename);
    // refes.addAll(bionexEvents);

    // move some ref to co
    Iterator<Event> refItr = refes.iterator();
    while (refItr.hasNext()) {
      Event e = refItr.next();
      
      if (e.comment.startsWith("# A is a B")
          || e.comment.startsWith("# NP , denoted NP")
          || e.comment.startsWith("# apposition")) 
          
      {
        corefes.add(e);
        refItr.remove();
      }
      
    }
    // read ENTITY-string and entity mapping
    String mapText = dir + "/" + filename + ".entmap";
    File file = new File(mapText);

    try {
    	String mapstr = FileUtils.readFileToString(file);
    	Gson gson = new Gson();
    	Type mapType = new TypeToken<HashMap<String,HashMap<String,String>>>() {}.getType();
    	map = gson.fromJson(mapstr, mapType);
    } catch (Exception e) {
    	System.exit(1);
    }
  }

  @Override
  public void processFile(String dir, String filename) {
    super.processFile(dir, filename);
    link();
    printA(dir, filename, "a7");
  }

  protected void link() {
    a7es = new LinkedList<Event>();
   
    // co
    if (linkCo || linkCoGold) {
      List<Event> a3esCo = new ArrayList<Event>();
     
      for (Event e : a3es) {
        linkCo(e, a3esCo, corefes);
      }
      a7es.addAll(a3esCo);
    }
    // ref
    if (linkRef) {
      List<Event> a3esRef = new ArrayList<Event>();
      
      for (Event e : a3es) {
        linkRef(e, a3esRef, refes);
      }
      a7es.addAll(a3esRef);
    }
    // a7es.addAll(a3es);
  }

  protected void linkCo(Event event, List<Event> linkedEvents,
      List<Event> refEvents) {

    currentEvent = event;
    

    // String argStr = text.substring(event.argument.from(),
    // event.argument.to());
    // if (!argStr.equalsIgnoreCase("its")
    // && !argStr.equalsIgnoreCase("it")
    // && !argStr.equalsIgnoreCase("their")
    // && !argStr.equalsIgnoreCase("they")) {
    // return;
    // }

    List<Entity> arguments = findAnotherEntityInCo(event.argument);
    for (Entity argument : arguments) {
      if (argument.equals(event.argument)) {
        continue;
      }
      // a1ts
      Entity foundEntity = null;
      for (Entity a1t : a1ts) {
        //if (a1t.to() == argument.to()) {
    	  if (entityInArgument(a1t,argument)) {
          //foundEntity = a1t;
    		  foundEntity = argument;
          break;
        }
      }
      if (foundEntity != null) {
        Event linkedE = new Event(event.filename, event.type, event.trigger,event.argumentType,
            foundEntity, event.comment);
        linkedEvents.add(linkedE);
      } else {
        Event linkedE = new Event(event.filename, event.type, event.trigger,event.argumentType,
            argument, event.comment);
        linkedEvents.add(linkedE);
      }
    }
  }

  protected void linkRef(Event event, List<Event> linkedEvents,
      List<Event> refEvents) {
	  
    currentEvent = event;
    String comment = event.comment;
	if(!comment.endsWith("-REF"))
	    comment += "-REF";
	
    for (Entity a1t : a1ts)
    {
    	if(a1t.range().equals(currentEvent.argument.range()))
    		return;
    }
    
    // get equal entities with argument by reference linking
    List<Entity> arguments = findAnotherEntityInRef(event.argument);
    
    // create linked event for each entity obtained by reference linking 
    for (Entity argument : arguments) {      
      if (argument.equals(event.argument)) {
        continue;
      }
      // a1ts
      Entity foundEntity = null;
      
      // check if the linked entity is in known entities list a1ts (bionlp a1 file)
      for (Entity a1t : a1ts) {
        //if ((a1t.to() == argument.to()) && (a1t.from() == argument.from())) {
    	  if(entityInArgument(a1t,argument)) {

    		  //foundEntity = a1t;
    		  foundEntity = argument;
    		  break;
        }
      } 
      
      // if it's in bionlp a1 file, then create linked event for the linked entity
      if (foundEntity != null) {

        Event linkedE = new Event(event.filename, event.type, event.trigger,event.argumentType,
            argument, comment);
        
        linkedEvents.add(linkedE);
        
      } 
      
      // if it's not in a1 file, continue to find further linked entity by reference linking
      else {
        Event linkedE = new Event(event.filename, event.type, event.trigger,event.argumentType,
            argument, comment);
        if(!linkedEvents.contains(linkedE))
        {
        	linkedEvents.add(linkedE);
        	try {
        		linkRef(linkedE, linkedEvents, refEvents);
        	} catch (StackOverflowError e) {
        		//System.err.println(filename);
        		System.err.println(event);
        		System.err.println(argument);
        		System.exit(1);
        	}
        }
      }
    }
  }

  protected List<Entity> findAnotherEntityInCo(Entity argument) {
    Set<Entity> set = new HashSet<Entity>();
    findAnotherEntityInCo(argument, set);
    List<Entity> cos = new ArrayList<Entity>(set);
    return cos;
  }

  protected void findAnotherEntityInCo(Entity argument, Set<Entity> set) {
    for (Event e : corefes) {
      try {
        if (e.trigger.to() == argument.to()
            && e.trigger.from() == argument.from()
            && !contains(set, e.argument)) {
          set.add(e.argument);
          
          findAnotherEntityInCo(e.argument, set);
        }
        if (//e.argument.to() == argument.to()
             e.argument.from() == argument.from()
            && !contains(set, e.trigger)) {
          set.add(e.trigger);
          findAnotherEntityInCo(e.trigger, set);
        }
      } catch (NoSuchElementException exp) {
        System.err.println(filename);
        System.err.println(e);
        System.err.println(argument);
        System.err.println(currentEvent);
        exp.printStackTrace();
        System.exit(1);
      }
    }
  }

  private boolean contains(Set<Entity> set, Entity argument) {
    for (Entity e : set) {
      if (e.to() == argument.to()) {
        return true;
      }
    }
    return false;
  }

  protected List<Entity> findAnotherEntityInRef(Entity argument) {
    List<Entity> refs = new ArrayList<Entity>();
    for (Event e : refes) {
      try {
        if (e.trigger.to() == argument.to() && e.trigger.from() == argument.from()) {
            if(!refs.contains(e.argument)) 
        	refs.add(e.argument);
        }
        if (e.argument.to() == argument.to() && e.argument.from() == argument.from()) {
        	if(!refs.contains(e.trigger))
        		
        	refs.add(e.trigger);
        }
        	
      } catch (NoSuchElementException exp) {
        System.err.println(filename);
        System.err.println(e);
        System.err.println(argument);
        System.err.println(currentEvent);
        exp.printStackTrace();
        System.exit(1);
      }
    }
    return refs;
  }
  
  protected boolean entityInArgument(Entity entity, Entity argument)
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
    			System.out.println(needle);
    			System.exit(1);
    		}
    	}
    	return false;
  }
  
  protected void printA(String dir, String filename, String ext) {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(dir
          + "/"
          + filename
          + "."
          + ext));
      for (Event e : a7es) {
        out.println(e);
      }
      out.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
