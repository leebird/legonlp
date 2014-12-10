package mods.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.ptb.PtbReader;
import mods.ptb.PtbString;
import mods.extractor.pattern.BioNLPPatternFactory;
import mods.extractor.pattern.ExtractorMatcher;
import mods.extractor.pattern.ExtractorPattern;
import mods.extractor.pattern.PAPatternReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;

public class ExtractorPatternTest extends FileProcessor {

  public static void main(String args[])
      throws IOException {

    List<ExtractorPattern> patterns = PAPatternReader.getPatterns(
        Env.BASIC_PATTERN_TREGEX,
        Env.NULL_PATTERN_TREGEX,
        BioNLPPatternFactory.instance(),
        "Vadj Arg");
    ExtractorPatternTest test = new ExtractorPatternTest(patterns, "Vadj Arg");
    test.processFile(Env.DIR, "PMC-1310901-05-MATERIALS_AND_METHODS-04");
  }

  Treebank               treebank;
  PrintStream            out;
  List<ExtractorPattern> patterns;
  int                    index;

  ExtractorPatternTest(List<ExtractorPattern> patterns, String name)
      throws FileNotFoundException {
    this.patterns = patterns;
    String filename = Env.DIR + "." + name + ".tree";
    out = new PrintStream(new FileOutputStream(filename));
    index = 0;
  }

  @Override
  protected void readResource(String dir, String filename) {
    super.filename = filename;
    PtbReader ptbReader = new PtbReader(dir + "/mccc/ptb2/" + filename + ".ptb");
    treebank = ptbReader.readTreebank();
  }

  @Override
  public void processFile(String dir, String filename) {
    System.out.println(filename);
    readResource(dir, filename);

    if (filename.equals("PMC-1310901-05-MATERIALS_AND_METHODS-04")) {
      // System.err.println();
    }

    for (Tree t : treebank) {
      for (ExtractorPattern pattern : patterns) {
        ExtractorMatcher m = pattern.matcher(t);
        while (m.find()) {
          Tree matched = m.getMatch();
          Tree trigger = m.trigger();
          Tree argument = m.argument();

          printSubtree(
              m.pattern() + ": " + m.tregexPattern(),
              matched,
              trigger,
              argument);
        }
      }
    }
  }

  private
      void
      printSubtree(String name, Tree root, Tree trigger, Tree argument) {
    List<Tree> highlightTrees = new ArrayList<Tree>();
    List<String> highlightNames = new ArrayList<String>();

    highlightTrees.add(trigger);
    highlightNames.add("trigger");

    highlightTrees.add(argument);
    highlightNames.add("argument");

    out.println(index
        + ". "
        + filename
        + "\n"
        + PtbString.pennString(root, highlightTrees, highlightNames));
    index++;
  }
}
