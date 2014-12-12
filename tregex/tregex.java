import java.util.Hashtable;
import java.util.ArrayList;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import com.google.gson.Gson;

class Input {
    public String parse;
    public String tregex;
    public String[] labels;
}

// running java Tregex 100 times is much slower than running main() 100 times internally
class Tregex {
    public static void main(String[] args) {

        Gson gson = new Gson();
        if(args.length == 0) {
            System.out.println(gson.toJson(false));
            return;
        }

        String input = args[0];
        ArrayList<Hashtable<String, String>> results = new ArrayList<Hashtable<String, String>>();

        // decode the json input

        Input data = gson.fromJson(input, Input.class);
        String parse = data.parse;
        String tregex = data.tregex;
        String[] labels = data.labels;

        // match the tree with tregex
        // Tree tree = Tree.valueOf("(S1 (S (S (NP (PRP I)) (VP (VBP have) (NP (DT a) (NN book)))) (. .)))");
        Tree tree = Tree.valueOf(parse);
        TregexPattern pattern = TregexPattern.compile(tregex);
        TregexMatcher matcher = pattern.matcher(tree);

        while(matcher.find()) {
            Hashtable<String, String> singleMatch = new Hashtable<String, String>();
            for(String label : labels) {
                Tree node = matcher.getNode(label);
                singleMatch.put(label, node.toString());
            }
            results.add(singleMatch);
        }
        // output results to stdout
        System.out.println(gson.toJson(results));
    }
}
