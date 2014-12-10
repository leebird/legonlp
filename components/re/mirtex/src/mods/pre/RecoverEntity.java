package mods.pre;
import mods.ptb.PtbReader;
import mods.utils.FileProcessor;
import mods.annotation.Entity;
import mods.annotation.Token;
import mods.annotation.A1EntityReader;
import mods.utils.Env;
import mods.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.Tree;

public class RecoverEntity extends FileProcessor{

	public String recover(String text, List<Entity> entityList, HashMap<String,HashMap<String,Integer>> map)
	{
        PtbReader ptbReader = new PtbReader(Env.DIR_PARSE + filename + ".ptb");
        Treebank treebank = ptbReader.readTreebank();
        String res = "";
        String pattern = Env.ENTITY_REPLACE;

        for(Tree t : treebank)
        {      
        	List<Tree> leaves = t.getLeaves();
        	
    		for(Tree l : leaves)
    		{        	 
    			String word = Utils.adaptValue(l.label().toString());
    	    	Pattern r = Pattern.compile(pattern);
    	    	Matcher m = r.matcher(word);
    	    	
    	    	while(m.find())
    	    	{
    	    		String needle = m.group();
    	    		try {
    	    			int entStart = map.get(needle).get("start");
    	    			int entend = map.get(needle).get("end");

	    			for(Entity entity : entityList)
	    			{	        
	    				
	        			int start = entity.from();
	        			int end = entity.to();
        				String entityText = entity.getText();

        				if(start == entStart && entend == end)
	        			{
        					word = word.replace(needle, entityText);
        					m = r.matcher(word);
        					break;
	        			}
	        		}
    	    		} catch (Exception e){
    	    			System.out.println(needle);
    	    			System.exit(1);
    	    		}
    	    	}
    	    	l.setValue(word);
    		}
        	res += t.toString()+"\n";
        }
        return res;
	}
	
    public String recover(String text,List<Entity> entityList)
    {
        PtbReader ptbReader = new PtbReader(Env.DIR_PARSE + filename + ".ptb");
        Treebank treebank = ptbReader.readTreebank();
        String res = "";
        
        for(Tree t : treebank)
        {      
        	for(Entity entity : entityList)
        	{
        		int textIndex = -1;
        		int textTo = -1;

        		List<Tree> leaves = t.getLeaves();

        		for(Tree l : leaves)
        		{
        			String word = Utils.adaptValue(l.label().toString());
        			textIndex = text.indexOf(word,textTo);
        			textTo = textIndex + word.length();

        			int start = entity.from();
        			int end = entity.to();

        			if(textIndex == -1)
        				continue;
        			
        			if(start >= textIndex && end <= textTo)
        			{
        				String entityText = "";    		
        				for(Token token : entity.tokens)    		
        					entityText += token.word + " ";    		    		
        				entityText = entityText.trim();


        				int innerStart = start - textIndex;
        				int innerEnd = innerStart + entityText.length();

        				String newWord = word.substring(0,innerStart) + 
        						entityText + 
        						word.substring(innerEnd,word.length());

        				l.setValue(newWord);
        				text = text.substring(0,textIndex)+newWord+text.substring(textTo,text.length());
        				break;
        			}
        		}
        	}
        	res += t.toString();

        }
        return res;
    }

    @Override
    public void readResource(String dir, String filenamee)
    {
        String rawText = dir + "/" + filename + ".txt";
        String entityText = dir + "/" + filename + ".a1";
        String originalText = dir + "/" + filename + ".ori";
        String parsedText = Env.DIR_PARSE + filename + ".ptb";
        String recoveredParsedText = Env.DIR_PARSE + filename + ".ptb.recovered";
        String mapText = dir + "/" + filename + ".entmap";
        
        // get replaced text with N string
        String replacedText = "";
        File file = new File(rawText);
        try {
        	replacedText = FileUtils.readFileToString(file);
        }
        catch(Exception e) {
            System.exit(1);
        }
        
        // recover original text
        String content = "";
        file = new File(originalText);
        try {
            content = FileUtils.readFileToString(file);
        }
        catch(Exception e) {
            System.exit(1);
        }
        
        try {
            PrintStream out = new PrintStream(new FileOutputStream(rawText));
            out.println(content);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        // read entities
        List<Entity> entityList = A1EntityReader.readEntities(entityText);
        
        // get parse trees
        file = new File(parsedText);
        try {
            content = FileUtils.readFileToString(file);
        }
        catch(Exception e) {
            System.exit(1);
        }
        
        String mapstr = "";
        file = new File(mapText);
        HashMap<String,HashMap<String,Integer>> map = new HashMap<String,HashMap<String,Integer>>();
        try {
        	mapstr = FileUtils.readFileToString(file);
        	Gson gson = new Gson();
        	Type mapType = new TypeToken<HashMap<String,HashMap<String,Integer>>>() {}.getType();
        	map = gson.fromJson(mapstr, mapType);
        } catch (Exception e) {
        	System.exit(1);
        }
        
        // recover parse tree with entities
        String res = recover(replacedText,entityList,map);
        
        // output new recovered parse tree
        try {
            PrintStream out = new PrintStream(new FileOutputStream(recoveredParsedText));
            out.println(res);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}