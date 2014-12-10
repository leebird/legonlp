package pengyifan.bionlpst.v3.extractor.pattern;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RefPatternReader {

  static boolean                read = false;
  static List<ExtractorPattern> patterns;

  public static List<ExtractorPattern> getPatterns(String refFilename,
      String memberCollectionFilename, String partWholeFilename,
      PatternFactory pf) {
    if (!read) {
      readPatterns(refFilename, memberCollectionFilename, partWholeFilename, pf);
    }
    return patterns;
  }

  public static List<ExtractorPattern> getPatterns(String refFilename,
      String memberCollectionFilename, String partWholeFilename,
      PatternFactory pf, String name) {
    if (!read) {
      readPatterns(refFilename, memberCollectionFilename, partWholeFilename, pf);
    }
    List<ExtractorPattern> subpatterns = new ArrayList<ExtractorPattern>();
    for (ExtractorPattern pattern : patterns) {
      if (pattern.getName().equals(name)) {
        subpatterns.add(pattern);
      }
    }
    return subpatterns;
  }

  private static void readPatterns(String refFilename,
      String memberCollectionFilename, String partWholeFilename,
      PatternFactory pf) {
    read = true;
    patterns = new ArrayList<ExtractorPattern>();
    try {
      // other
      ExtractorPatternReader reader = new ExtractorPatternReader(
          new FileReader(refFilename), pf);
      ExtractorPattern pattern = null;
      while ((pattern = reader.readPattern()) != null) {
        patterns.add(pattern);
      }
      reader.close();
      // member-collection
      reader = new ExtractorPatternReader(new FileReader(
          memberCollectionFilename), pf);
      while ((pattern = reader.readPattern()) != null) {
        patterns.add(pattern);
      }
      reader.close();
      // part-whole
      reader = new ExtractorPatternReader(new FileReader(partWholeFilename), pf);
      while ((pattern = reader.readPattern()) != null) {
        patterns.add(pattern);
      }
      reader.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
