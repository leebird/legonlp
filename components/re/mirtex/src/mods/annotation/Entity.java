package mods.annotation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mods.utils.Env;
import mods.utils.Utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.lang3.Range;

public class Entity implements Comparable<Entity> {

  final public String             id;
  final public String             type;
  final public LinkedList<Token> tokens;
  final private Range<Integer>    range;
  public int direction;
  public int sid;
  
  public Entity(String id, String type, List<Token> tokens) {
    this.id = id;
    this.type = type;
    this.tokens = new LinkedList<Token>(tokens);
    this.range = Range.between(from(), to());
  }

  public final int from() {
    return getFirst().from();
  }

  public final int to() {
    return getLast().to();
  }

  public final int offset() {
    return to() - from();
  }

  public final Range<Integer> range() {
    return range;
  }

  public final Token getFirst() {
	  return tokens.getFirst();
  }

  public final Token getLast() {
    return tokens.getLast();
  }

  @Override
  public String toString() {
    return String.format("T\t%s\t%s", type, tokens);
  }

  @Override
  public boolean equals(Object obj) {
    Entity e = (Entity) obj;
    return tokens.equals(e.tokens);
  }
  
  @Override
  public int hashCode() {
	  int hash = this.tokens.toString().hashCode();
	  return hash;
  }
  
  public static Entity find(List<Entity> list, Entity e) {
    return CollectionUtils.find(list, PredicateUtils.equalPredicate(e));
  }

  public String getText() {
	  String res = "";
	  for(Token t : this.tokens)
		  res += Utils.adaptValue(t.word) + " ";
	  return res.trim();
  }
  
  // recover original text of entity
  public String getRepText(Entity entity) {
	  String res = "";
	  for(Token t : this.tokens)
	  {
		  if(t.range.containsRange(entity.range()))
		  {
			  int start = entity.from() - t.from();
			  int end = entity.to() - t.from();
			  String rep = t.word.substring(0, start) + entity.getText() + t.word.substring(end, t.word.length());
			  res += rep + " "; 
		  }
		  else
			  res += Utils.adaptValue(t.word) + " ";
	  }
	  return res.trim();
  }
  
  public String getRepText(Entity entity, HashMap<String,HashMap<String,String>> map) {
		
	  String word = this.getText();
	  String pattern = Env.ENTITY_REPLACE;
	  Pattern r = Pattern.compile(pattern);
	  Matcher m = r.matcher(word);
	  
	  int start = entity.from();
	  int end = entity.to();
	  
	  while(m.find())
	  {
		  String needle = m.group();
		  try {
			  int entStart = Integer.parseInt(map.get(needle).get("start"));
			  int entend = Integer.parseInt(map.get(needle).get("end"));

			  if(start == entStart && entend == end)
			  {
				  word = word.replace(needle, entity.getText());
				  break;
			  }
		  } catch (Exception e) {
			  System.out.println(needle);
			  System.exit(1);
		  }
	  }
	  
	  return word;
  }
  

  @Override
  public int compareTo(Entity e) {
    int c = new Integer(from()).compareTo(e.from());
    if (c == 0) {
      c = new Integer(to()).compareTo(e.to());
    }
    return c;
  }
}
