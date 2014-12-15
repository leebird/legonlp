package tregex;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.BufferedWriter;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.ArrayUtils;

import tregex.ptb.OffsetTree;
import tregex.ptb.OffsetTreeFactory;
import tregex.ptb.OffsetLabelFactory;

class Input {
    public Hashtable<String, ArrayList<ArrayList<String>>> input;
    public Hashtable<String, ArrayList<ArrayList<String>>> output;
    public String[] doc_list;
    public String[] pattern_files;
}

class Pattern {
    public String id;
    public String tregex;
    public String name;
    public String category;
    public String comment;
    public String[] labels;
}

/*
running java Tregex 100 times is much slower than running main() 100 times internally
processing a directory at a time should be faster
*/

class Tregex {

    // public static Gson gson = new Gson();
    // pretty print, do not escape for html (e.g., <, >)

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

        // read all patterns in the pattern file
        String content;
        Pattern[] patterns = new Pattern[0];

        try {
            for (String patternFile : data.pattern_files) {
                content = new String(Files.readAllBytes(Paths.get(patternFile)));
                patterns = ArrayUtils.addAll(patterns, gson.fromJson(content, Pattern[].class));
            }
        } catch (Exception e) {
            System.err.println("Can not open pattern file");
            System.err.println(e);
            return;
        }

        String inputDir = data.input.get("parse").get(0).get(0);
        String inputSux = data.input.get("parse").get(0).get(1);
        String outputDir = data.output.get("tregex").get(0).get(0);
        String outputSux = data.output.get("tregex").get(0).get(1);

        for (String doc : data.doc_list) {

            // read all parses in the parse file
            String filepath = inputDir + "/" + doc + inputSux;
            try {
                content = new String(Files.readAllBytes(Paths.get(filepath)));
                String[] parses = gson.fromJson(content, String[].class);

                String result = match(parses, patterns);
                // System.out.println(result);

                // write results to output file
                String outputFile = outputDir + "/" + doc + outputSux;
                try {
                    Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(outputFile), "utf-8"));
                    writer.write(result);
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Can not write to file at ");
                }
            } catch (Exception e) {
                System.err.println("Can not open doc file at " + filepath);
            }
        }
    }

    public static String match(String[] parses, Pattern[] patterns) {

        ArrayList<Hashtable<String, String>> results = new ArrayList<Hashtable<String, String>>();

        // match the tree with tregex
        // loop through all parses and all patterns

        for (String parse : parses) {

            // normal tree
            // Tree tree = Tree.valueOf(parse);

            // offset tree
            Tree tree = Tree.valueOf(parse);
            //OffsetTree tree2 = new OffsetTree(tree.label(), Arrays.asList(tree.children()));
            //System.out.println(tree2.getLeaves());

            for (Pattern pattern : patterns) {

                TregexPattern tregexPattern = TregexPattern.compile(pattern.tregex);
                TregexMatcher tregexMatcher = tregexPattern.matcher(tree);

                while (tregexMatcher.find()) {
                    Hashtable<String, String> singleMatch = new Hashtable<String, String>();
                    singleMatch.put("pattern_category", pattern.category);
                    singleMatch.put("pattern_id", pattern.id);
                    singleMatch.put("pattern_tregex", pattern.tregex);
                    singleMatch.put("pattern_name", pattern.name);

                    for (String label : pattern.labels) {
                        Tree node = tregexMatcher.getNode(label);
                        singleMatch.put(label, node.toString());
                    }
                    results.add(singleMatch);
                }
            }
        }
        // output results to stdout
        return gson.toJson(results);
    }
}
