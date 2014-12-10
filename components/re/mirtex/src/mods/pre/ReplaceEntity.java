package mods.pre;
import mods.utils.FileProcessor;
import mods.annotation.Entity;
import mods.annotation.A1EntityReader;
import mods.annotation.Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;

public class ReplaceEntity extends FileProcessor{


    public String replace(String text,List<Entity> entityList)
    {    	
    	for(Entity entity : entityList)
    	{
    		Integer start = entity.from();
    		Integer end = entity.to();
    		Integer length = end - start;
    		
    		String alter = "";
    		for(Integer i = 0; i < length; i++)
    			alter += "N";
    		
    		text = text.substring(0,start)+alter+text.substring(end,text.length());
    	}
    	
    	return text;
    }
    
    @SuppressWarnings("unchecked")
	public String replace_2(String dir, String text, List<Entity> entityList)
    {
    	List<Range<Integer>> rangeList = new ArrayList<Range<Integer>>();
    	HashMap<String,HashMap<String,String>> a3map= new HashMap<String,HashMap<String,String>>();
    	
    	Collections.sort(entityList);

    	int count = 0;
    	int point = 0;
    	List<String> snippets = new ArrayList<String>();
    	
    	for(Entity entity : entityList)
    	{
    		int start = entity.from();
    		int end = entity.to();
//    		rangeList.add(Range.between(start, end));
//    	}
//    	
//    	for(Range<Integer> range : rangeList)
//    	{
    		try {
    			count++;
    			String needle = "N"+Integer.toString(count)+"ENTITY";
    			snippets.add(text.substring(point,start));
    			snippets.add(needle);
    			point = end;
    			HashMap<String,String> posmap= new HashMap<String,String>();
    			posmap.put("start", Integer.toString(start));
    			posmap.put("end", Integer.toString(end));
    			posmap.put("type", entity.type);
    			posmap.put("text", entity.getText());
    			a3map.put(needle, posmap);
    		}
    		catch(Exception e) {
    			System.out.println(e);
    			System.out.println(filename+":"+Integer.toString(point)+":"+Integer.toString(start)+"@"+text.length());
    		}
    	}
    	saveMap(dir, a3map);
    	snippets.add(text.substring(point,text.length()));
    	return StringUtils.join(snippets,"").trim();
    }
    
    public void saveMap(String dir, HashMap<String,HashMap<String,String>> map)
    {
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	Type mapType = new TypeToken<HashMap<String,HashMap<String,String>>>() {}.getType();
    	String jsonOutput = gson.toJson(map,mapType);
        try {
            PrintStream out = new PrintStream(new FileOutputStream(dir + "/" + filename + ".entmap"));
            out.println(jsonOutput);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public void readResource(String dir, String filenamee)
    {
        String rawText = dir + "/" + filename + ".txt";
        String entityText = dir + "/" + filename + ".a1";
        String originalText = dir + "/" + filename + ".ori";

        String content = "";
        File file = new File(rawText);
        try {
            content = FileUtils.readFileToString(file,StandardCharsets.UTF_8);
        }
        catch(Exception e) {
            System.exit(1);
        }
        
        try {
            PrintStream out = new PrintStream(new FileOutputStream(originalText));
            out.println(content);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        List<Entity> entityList = A1EntityReader.readEntities(entityText);
        
        String res = replace_2(dir, content,entityList);
        
        try {
            PrintStream out = new PrintStream(new FileOutputStream(rawText));
            out.println(res);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}