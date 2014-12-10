package mods.pos;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.utils.Utils;
import mods.annotation.A1EntityReader;
import mods.annotation.A3EventReader;
import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Event.ArgumentType;
import mods.annotation.Event.EventType;

public class LinkCausal extends FileProcessor {

  /**
   * @param args
   */
  public static void main(String[] args) {
	  LinkCausal p = new LinkCausal(true);

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

  public LinkCausal(boolean linkRef) {
    this.linkRef = linkRef;
  }

  @Override
  protected void readResource(String dir, String filename) {
    // read text
    text = Utils.readText(dir + "/" + filename + ".txt");
    // a1
    a1ts = A1EntityReader.readEntities(dir + "/" + filename + ".a1");
    // a3
    a3es = A3EventReader.readEvents(dir + "/" + filename + ".a3.Regulation");
    
    a3es.addAll(A3EventReader.readEvents(dir + "/" + filename + ".a3.MiRNATarget"));
    
    // ref
    refes = new ArrayList<Event>();
    if (linkRef) {
      String reffilename = Env.DIR_REF + filename + ".causal";
      refes = A3EventReader.readEvents(reffilename);
    }
  }

  @Override
  public void processFile(String dir, String filename) {
    super.processFile(dir, filename);
    link();
    printA(dir, filename, "a3.Causal");
  }

  protected void link() {
    a7es = new LinkedList<Event>();
   
    // ref
    if (linkRef) {
      List<Event> a3esRef = new ArrayList<Event>();

      for (Event e : refes) {
        linkRef(e, a3esRef);
      }

      a7es.addAll(a3esRef);
    }
  }

  protected void linkRef(Event event, List<Event> linkedEvents) {
	  
  	List<Entity> causeList = new ArrayList<Entity>();
  	List<Entity> effectList = new ArrayList<Entity>();
  	
	if(event.argumentType == Event.ArgumentType.Agent)
		causeList.add(event.argument);
	if(event.argumentType == Event.ArgumentType.Theme)
		effectList.add(event.argument);
  	
	/*
    for(Event e : a3es)
    {

    	
    	if(event.argumentType == Event.ArgumentType.Agent &&
    		event.argument.range().containsRange(e.argument.range()))
    		causeList.add(e.argument);
    	
    	if(event.argumentType == Event.ArgumentType.Theme &&
        	event.argument.range().containsRange(e.argument.range()))
        		effectList.add(e.argument);
    }
    for(Entity e : a1ts)
    {
    	if(event.argumentType == Event.ArgumentType.Agent &&
        		event.argument.range().containsRange(e.range()))
        		causeList.add(event.argument);
        	
        	if(event.argumentType == Event.ArgumentType.Theme &&
            	event.argument.range().containsRange(e.range()))
            		effectList.add(event.argument);
    }
    */
    for(Entity c : causeList)
    {
    	Event causalRel = new Event(filename, EventType.Event, event.trigger,
    			ArgumentType.Agent, c, event.comment);
				linkedEvents.add(causalRel);
    }
	for(Entity r : effectList)
	{
		Event causalRel = new Event(filename, EventType.Event, event.trigger,
		            ArgumentType.Theme, r, event.comment);
		linkedEvents.add(causalRel);
	}
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
