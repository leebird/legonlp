import static org.junit.Assert.*;

import banner.Sentence;
import banner.BannerProperties;
import banner.tagging.Mention;
import banner.tagging.MentionType;
import banner.tagging.CRFTagger;
import banner.tokenization.SimpleTokenizer;
import banner.tokenization.Tokenizer;
import banner.processing.ParenthesisPostProcessor;
import banner.processing.PostProcessor;
import java.util.List;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class gm {
    public static void main(String[] args) {        
        String fromDir = args[0];
        String suffix = args[1];
        List<String> docList = Arrays.asList(args[2].split(","));

        HashMap<String,String> res = new HashMap<String,String>();

        try {            

            // disable stdout
            PrintStream original = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                    public void write(int b) {
                        //DO NOTHING
                    }
                }));

            // disable stderr

            System.setErr(new PrintStream(new OutputStream() {
                    public void write(int b) {
                        //DO NOTHING
                    }
                }));

            // use stanford corenlp to split the sentences for banner
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            // banner setup
            String propertyPath = "banner.properties";
            String modelPath = "gene_model_v02.bin";
            BannerProperties properties = BannerProperties.load(propertyPath);
            Tokenizer tokenizer = properties.getTokenizer();

            CRFTagger tagger = CRFTagger.load(new File(modelPath), 
                                              properties.getLemmatiser(), 
                                              properties.getPosTagger());

            // gson setup
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Type mapType = new TypeToken<HashMap<String,String>>() {}.getType();

            for(String doc : docList)
            {
                String fromFile = fromDir + "/" + doc + suffix;
                File fileDir = new File(fromFile);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        new FileInputStream(fileDir), "UTF8"));

                String str;
                String text = "";

                while ((str = in.readLine()) != null) 
                {
                    text += str + " ";
                }

                text = text.trim();
                in.close();

                // create an empty Annotation just with the given text
                Annotation document = new Annotation(text);
                
                // run all Annotators on this text
                pipeline.annotate(document);
    
                // these are all the sentences in this document
                // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
                List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    
                String annotated = "";

                for(CoreMap sent: sentences) {
                    String sentenceText = sent.toString();
                    Sentence sentence = new Sentence(sentenceText);
                    tokenizer.tokenize(sentence);
                    tagger.tag(sentence);
                    String annotatedSen = sentence.getSGML();
                    annotatedSen = annotatedSen.replace("<GENE> ","<GENE>");
                    annotatedSen = annotatedSen.replace(" </GENE>","</GENE>");
                    annotatedSen = annotatedSen.trim();
                    annotated += annotatedSen + "\n";
                }
                annotated = annotated.trim();
                res.put(doc,annotated);
            } 

            String jsonOutput = gson.toJson(res,mapType);
            System.setOut(original);
            System.out.println(jsonOutput);
        }
        catch(Exception e) {
            System.out.print("");
        }
    }
}
