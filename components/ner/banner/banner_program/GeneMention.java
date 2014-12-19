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

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.File;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

class Input {
    public Hashtable<String, ArrayList<ArrayList<String>>> input;
    public Hashtable<String, ArrayList<ArrayList<String>>> output;
    public String[] doc_list;
    public String[] pattern_files;
}

public class GeneMention {

    public static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println(gson.toJson(false));
            return;
        }

        // decode json input
        String input = args[0];
        Input data = gson.fromJson(input, Input.class);

        // process directory
        processDir(data);
    }

    public static void processDir(Input data) {

        String inputDir = data.input.get("text").get(0).get(0);
        String inputSux = data.input.get("text").get(0).get(1);
        String outputDir = data.output.get("ner").get(0).get(0);
        String outputSux = data.output.get("ner").get(0).get(1);
        String[] docList = data.doc_list;

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

            for (String doc : docList) {
                String inputFile = inputDir + "/" + doc + inputSux;
                String text = new String(Files.readAllBytes(Paths.get(inputFile)));

                String outputFile = outputDir + "/" + doc + outputSux;
/*
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(inFile), "UTF8"));

                String str;
                String text = "";

                while ((str = in.readLine()) != null) {
                    text += str + " ";
                }

                text = text.trim();
                in.close();*/

                // create an empty Annotation just with the given text
                Annotation document = new Annotation(text);

                // run all Annotators on this text
                pipeline.annotate(document);

                // these are all the sentences in this document
                // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
                List<CoreMap> sentences = document.get(SentencesAnnotation.class);

                String annotated = "";

                for (CoreMap sent : sentences) {
                    String sentenceText = sent.toString();
                    Sentence sentence = new Sentence(sentenceText);
                    tokenizer.tokenize(sentence);
                    tagger.tag(sentence);
                    String annotatedSen = sentence.getSGML();
                    annotatedSen = annotatedSen.replace("<GENE> ", "<GENE>");
                    annotatedSen = annotatedSen.replace(" </GENE>", "</GENE>");
                    annotatedSen = annotatedSen.trim();
                    annotated += annotatedSen + "\n";
                }

                annotated = annotated.trim();
                try {
                    Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(outputFile), "utf-8"));
                    writer.write(annotated);
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Can not write to file at ");
                }
            }
            System.setOut(original);
        } catch (Exception e) {
            System.out.print("");
        }
    }
}
