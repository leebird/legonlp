package mods.extractor.pattern;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PAPatternReader {

  static List<ExtractorPattern> allPatterns   = new ArrayList<ExtractorPattern>();
  static List<ExtractorPattern> basicPatterns = new ArrayList<ExtractorPattern>();
  static List<ExtractorPattern> nullPatterns  = new ArrayList<ExtractorPattern>();

  static List<ExtractorPattern> agentPatterns = new ArrayList<ExtractorPattern>();
  static List<ExtractorPattern> themePatterns = new ArrayList<ExtractorPattern>();

  public static List<ExtractorPattern> getPatterns(String basicPatternFilename,
      String nullPatternFilename, PatternFactory pf) {
    if (nullPatterns.isEmpty()) {
      readPatterns(nullPatternFilename, nullPatterns, pf,"null");
    }
    if (basicPatterns.isEmpty()) {
      readPatterns(basicPatternFilename, basicPatterns, pf,"basic");
    }
    // all
    allPatterns.clear();
    allPatterns.addAll(basicPatterns);
    allPatterns.addAll(nullPatterns);
    return allPatterns;
  }

    public static List<ExtractorPattern> getPatterns(String agentPatternFilename,
                                                     String themePatternFilename,
                                                     String nullPatternFilename,
                                                     PatternFactory pf) {
        if (nullPatterns.isEmpty()) {
            readPatterns(nullPatternFilename, nullPatterns, pf,"null");
        }

        if (agentPatterns.isEmpty()) {
            readPatterns(agentPatternFilename, agentPatterns, pf,"agent");
        }

        if (themePatterns.isEmpty()) {
            readPatterns(themePatternFilename, themePatterns, pf,"theme");
        }
        // all
        allPatterns.clear();
        allPatterns.addAll(agentPatterns);
        allPatterns.addAll(themePatterns);
        allPatterns.addAll(nullPatterns);
        return allPatterns;
    }

  public static List<ExtractorPattern> getBasicPatterns(
      String basicPatternFilename, PatternFactory pf) {
    if (basicPatterns.isEmpty()) {
      readPatterns(basicPatternFilename, basicPatterns, pf,"basic");
    }
    return basicPatterns;
  }

  public static List<ExtractorPattern> getNullPatterns(
      String nullPatternFilename, PatternFactory pf) {
    if (nullPatterns.isEmpty()) {
      readPatterns(nullPatternFilename, nullPatterns, pf,"null");
    }
    return basicPatterns;
  }

  public static List<ExtractorPattern> getPatterns(String basicPatternFilename,
      String nullPatternFilename, PatternFactory pf, String patternName) {
    getPatterns(basicPatternFilename, nullPatternFilename, pf);
    List<ExtractorPattern> subpatterns = new ArrayList<ExtractorPattern>();
    for (ExtractorPattern pattern : allPatterns) {
        if (pattern.getName().equals(patternName)) {
            subpatterns.add(pattern);
        }
    }
    return subpatterns;
  }

    // for agent, theme, null patterns
    public static List<ExtractorPattern> getPatterns(String agentPatternFilename,
                                                     String themePatternFilename,
                                                     String nullPatternFilename,
                                                     PatternFactory pf,
                                                     String patternName) {
        getPatterns(agentPatternFilename, themePatternFilename, nullPatternFilename, pf);

        List<ExtractorPattern> subpatterns = new ArrayList<ExtractorPattern>();

        for (ExtractorPattern pattern : allPatterns) {
            if (pattern.getName().equals(patternName)) {
                subpatterns.add(pattern);
            }
        }

        return subpatterns;
    }

    private static void readPatterns(String filename,
                                     List<ExtractorPattern> list, 
                                     PatternFactory pf,
                                     String argumentType) {

    try {
        ExtractorPattern pattern = null;
        // basic
        ExtractorPatternReader reader = new ExtractorPatternReader(
            new FileReader(filename), pf,argumentType);
        while ((pattern = reader.readPattern()) != null) {
            list.add(pattern);
        }
        reader.close();
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}
