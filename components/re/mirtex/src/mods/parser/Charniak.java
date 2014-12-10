package mods.parser;

import mods.utils.Env;
import mods.utils.FileProcessor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.List;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;
import org.apache.commons.io.FileUtils;
import java.io.File;

public class Charniak extends FileProcessor{

    private static final  String PARSER = "sh /home/leebird/nlp/parser/charniak/bllip-parser-master/parse.sh ";

    public void main()
        {
            Charniak p = new Charniak();
            p.readResource("","");
        }

    public String parse(String splitText)
        throws Exception {
        String parserOutput = "";
        String s = "";
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(PARSER+splitText);

        BufferedReader stdInput = new BufferedReader(new 
                                                     InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new 
                                                     InputStreamReader(proc.getErrorStream()));

        while ((s = stdInput.readLine()) != null) {
            parserOutput += s+"\n";
        }

        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
        return parserOutput;
    }

    @Override
    public void readResource(String dir, String filenamee)
        {
            String rawText = dir + "/" + filename + ".txt";
            String splitText = Env.DIR_PARSE + filename + ".split";
            String parsedText = Env.DIR_PARSE + filename + ".ptb";
            String errorText = Env.DIR_PARSE + filename + ".chk";

            String content = "";
            String split = "";
            String sentenceText = "";

            File file = new File(rawText);
            try {
                content = FileUtils.readFileToString(file);
            }
            catch(Exception e) {
                System.exit(1);
            }

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    
            // read some text in the text variable
            String text = content; // Add your text here!
    
            // create an empty Annotation just with the given text
            Annotation document = new Annotation(text);
    
            // run all Annotators on this text
            pipeline.annotate(document);
    
            // these are all the sentences in this document
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    
            for(CoreMap sentence: sentences) {
                // traversing the words in the current sentence
                sentenceText = sentence.toString();
                if(sentenceText.startsWith("AB - ") || sentenceText.startsWith("TI - "))
                    sentenceText = sentenceText.substring(5);
                split += "<s> " + sentenceText + " </s>\n";
            }

            try {
                PrintStream out = new PrintStream(new FileOutputStream(splitText));
                out.println(split);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }

            Charniak p = new Charniak();
            try {
                String res = p.parse(splitText);
                PrintStream out = new PrintStream(new FileOutputStream(parsedText));
                out.println(res);
                out.close();
            } catch (Exception e) {
                System.exit(1);
            }
        }
}

