package pengyifan.bionlpst.v3.extractor.pattern;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PAPatternReader {

  static List<ExtractorPattern> allPatterns   = new ArrayList<ExtractorPattern>();
  static List<ExtractorPattern> basicPatterns = new ArrayList<ExtractorPattern>();
  static List<ExtractorPattern> nullPatterns  = new ArrayList<ExtractorPattern>();

  public static List<ExtractorPattern> getPatterns(String basicPatternFilename,
      String nullPatternFilename, PatternFactory pf) {
    if (nullPatterns.isEmpty()) {
      readPatterns(nullPatternFilename, nullPatterns, pf);
    }
    if (basicPatterns.isEmpty()) {
      readPatterns(basicPatternFilename, basicPatterns, pf);
    }
    // all
    allPatterns.clear();
    allPatterns.addAll(basicPatterns);
    allPatterns.addAll(nullPatterns);
    return allPatterns;
  }

  public static List<ExtractorPattern> getBasicPatterns(
      String basicPatternFilename, PatternFactory pf) {
    if (basicPatterns.isEmpty()) {
      readPatterns(basicPatternFilename, basicPatterns, pf);
    }
    return basicPatterns;
  }

  public static List<ExtractorPattern> getNullPatterns(
      String nullPatternFilename, PatternFactory pf) {
    if (nullPatterns.isEmpty()) {
      readPatterns(nullPatternFilename, nullPatterns, pf);
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

  private static void readPatterns(String filename,
      List<ExtractorPattern> list, PatternFactory pf) {

    try {
      ExtractorPattern pattern = null;
      // basic
      ExtractorPatternReader reader = new ExtractorPatternReader(
          new FileReader(filename), pf);
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
