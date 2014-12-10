package mods.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.Range;

import mods.annotation.Event;
import mods.annotation.Event.EventType;
import mods.annotation.Token;
import mods.annotation.Trigger;
import mods.ptb.OffsetLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Trees;
import edu.stanford.nlp.util.Pair;

public class Utils {

  public static String handleRegStr(String s) {
    s = s.replaceAll("\\(", "\\\\(");
    s = s.replaceAll("\\)", "\\\\)");
    s = s.replaceAll("\\[", "\\\\[");
    s = s.replaceAll("\\]", "\\\\]");
    s = s.replaceAll("[.]", "[.]");
    s = s.replaceAll("[-]", "[-]");
    return s;
  }

  public static String adaptValue(String value) {
    value = value.replace("-LRB-", "(");
    value = value.replace("-RRB-", ")");
    value = value.replace("-LCB-", "{");
    value = value.replace("-RCB-", "}");
    value = value.replace("-LSB-", "[");
    value = value.replace("-RSB-", "]");
    value = value.replace("-lrb-", "(");
    value = value.replace("-rrb-", ")");
    value = value.replace("-lsb-", "[");
    value = value.replace("-rsb-", "]");
    value = value.replace("``", "\"");
    value = value.replace("''", "\"");
    return value;
  }

  public static String readText(String filename) {
    StringBuilder text = new StringBuilder();

    try {
      LineNumberReader reader = new LineNumberReader(new FileReader(filename));
      String line = null;
      while ((line = reader.readLine()) != null) {
        text.append(line + "\n");
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return text.toString();
  }

  static List<Trigger> triggers = null;

  public static List<Event> filter(List<Event> list, EventType type) {
    return new ArrayList<Event>(CollectionUtils.select(list, new TypePredicate(
        type)));
  }

  public static class TypePredicate implements Predicate<Event> {

    EventType type;

    public TypePredicate(EventType type) {
      this.type = type;
    }

    @Override
    public boolean evaluate(Event e) {
      return type == e.type;
    }

  }

  /**
   * Find the Lowest Common Ancestor of [from, to] in the tree t
   * 
   * @param t
   * @param e
   * @return null if nothing found
   */
  public static Pair<Tree, Tree> findTree(Tree t, Range<Integer> range) {
    Tree tnF = null;
    Tree tnT = null;
    for (Tree leaf : t.getLeaves()) {
      OffsetLabel label = (OffsetLabel) leaf.label();
      if (range.getMinimum() == label.beginPosition()) {
        tnF = leaf.parent(t);
      }
      if (range.getMaximum() == label.endPosition()) {
        tnT = leaf.parent(t);
      }
    }
    if (tnF == null || tnT == null) {
      return null;
    }
    return new Pair<Tree, Tree>(tnF, tnT);
  }

  public static Tree firstLeaf(Tree t) {
    return Trees.getLeaf(t, 0);
  }

  public static Tree lastLeaf(Tree t) {
    List<Tree> leaves = t.getLeaves();
    return leaves.get(leaves.size() - 1);
  }

  public static Range<Integer> getRange(Tree tree) {
    Tree firstLeaf = firstLeaf(tree);
    Tree lastLeaf = lastLeaf(tree);
    return Range.between(
        ((OffsetLabel) firstLeaf.label()).beginPosition(),
          ((OffsetLabel) lastLeaf.label()).endPosition());
  }

  public static List<Token> getTokens(Tree root, Tree tree) {
    return getSpan(root, Collections.singletonList(tree));
  }

 
  public static List<Token> getSpan(Tree root, List<Tree> treeList) {
      
	ArrayList<Token> tokens = new ArrayList<Token>();
	
	  for (Tree tree : treeList) {
		  
		  Iterator<Tree> it = tree.iterator();  
		  
		  while(it.hasNext()) {
			  Tree subTree = it.next();

			  //System.out.println(subTree);
			  
			  List<Tree> children = subTree.getChildrenAsList();

			  for (Tree child : children) {

				  if(child.isLeaf()) {

		    			OffsetLabel label = (OffsetLabel) child.label();
		        	  		
		                Token t = new Token(label.value(), //
		                      subTree.label().value(), //
		                      label.beginPosition(), //
		                      label.endPosition());
		                
		                tokens.add(t);
		                break;
		    		}
		    		else
		    			break;
		    	}
		  }
		  
	  }

    	
    	/*
    	List<Tree> leaves = tree.getLeaves();
      
      for (Tree leaf : leaves) {
    	 
        assert leaf.label() instanceof OffsetLabel : leaf;

        OffsetLabel label = (OffsetLabel) leaf.label();

        // assert leaf.parent(root) != null : tree;

        Token t = null;
        if (leaf.parent(root) == null) {
          t = new Token(label.value(), //
              "NP", //
              label.beginPosition(), //
              label.endPosition());
        } else {
          t = new Token(label.value(), //
              leaf.parent(root).value(), //
              label.beginPosition(), //
              label.endPosition());
        }
        tokens.add(t);
        */
  
    return tokens;
  }
}

