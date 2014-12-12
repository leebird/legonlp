import java.util.Hashtable;
import java.util.ArrayList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.ArrayUtils;

class Input {
    public String inputDir;
    public String outputDir;
    public String inputSux;
    public String outputSux;
    public String[] docList;
    public String[] patternFiles;
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

    //public static Gson gson = new Gson();
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

        System.out.println(data.patternFiles);
        try {
            for (String patternFile : data.patternFiles) {
                content = new String(Files.readAllBytes(Paths.get(patternFile)));
                patterns = ArrayUtils.addAll(patterns, gson.fromJson(content, Pattern[].class));
            }
        } catch (Exception e) {
            System.err.println("Can not open pattern file");
            return;
        }

        for (String doc : data.docList) {

            // read all parses in the parse file
            String filepath = data.inputDir + "/" + doc + data.inputSux;
            try {
                content = new String(Files.readAllBytes(Paths.get(filepath)));
                String[] parses = gson.fromJson(content, String[].class);

                String result = match(parses, patterns);
                System.out.println(result);

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

            Tree tree = Tree.valueOf(parse);

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
