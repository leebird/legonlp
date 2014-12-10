package mods.pre;
import mods.annotation.A1EntityReader;
import mods.annotation.Entity;
import mods.annotation.Token;
import mods.utils.FileProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class MiRNARecognizer extends FileProcessor{
	
	// from wikipedia 
	// miR-123
	// mir-123
	// microRNA-123
	// miRNA-123
	// miR-123a
	// hsa-miR-123
	// hsa-mir-123-1
	// v-miR-123
	// d-miR-123
	// miR-123*
	// miR-123-3p
	// miR-123-5p
	
	private String pattern = "(?<=(^|\\s|[^a-zA-Z]))([a-zA-Z0-9]+)?-?(miR|miRNA|microRNA)(-|\\s|x)?[0-9]+[a-zA-Z]?\\*?([-+][0-9][^\\s]*)?";
	
    public String recognize(String text, List<Entity> oldEntityList)
    {
    	Pattern r = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
    	Matcher m = r.matcher(text);
    	String entityList = "";
    	Integer index = 0;
    	
    	while(m.find())
    	{
    		index++;
    		String entity = m.group();
            Integer start = m.start();
            Integer end = entity.length() + start;
            entityList += "T"+index.toString()+"\t"+"MiRNA "+start.toString()+" "+end.toString()+"\t"+entity+"\n";
            findEntity(start,end,oldEntityList);
    	}
    	
    	/*
    	for(Entity e : oldEntityList)
    	{
    		index++;
    		String entityText = "";    		
    		for(Token token : e.tokens)    		
    			entityText += token.word + " ";    		    		
    		entityText = entityText.trim();
    		
    		entityList += "T"+index.toString()+"\t"+e.type+" "+Integer.toString(e.from())+" "+Integer.toString(e.to())+"\t"+entityText+"\n";
    	}
    	*/
    	
    	return entityList;
    }
    
    public boolean findEntity(Integer start, Integer end, List<Entity> entityList)
    {
    	for(Entity e : entityList)
    	{
    		if(e.from()==start && e.to()==end)
    		{
    			// don't remove duplicate for now
    			//entityList.remove(e);
    			return true;
    		}	
    	}
    	return false;
    }
    
    @Override
    public void readResource(String dir, String filenamee)
    {
        String rawText = dir + "/" + filename + ".txt";
        String entityText = dir + '/' + filename + ".a1";

        String entityList = "";
        String content = "";
        
        File file = new File(rawText);
        try {
            content = FileUtils.readFileToString(file);
            //System.out.println(content);
        }
        catch(Exception e) {
            System.exit(1);
        }
        
        List<Entity> oldEntityList = A1EntityReader.readEntities(entityText);
        
        entityList = recognize(content,oldEntityList);
        entityList = entityList.trim();
        
        try {
            PrintStream out = new PrintStream(new FileOutputStream(entityText));
            out.println(entityList);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}