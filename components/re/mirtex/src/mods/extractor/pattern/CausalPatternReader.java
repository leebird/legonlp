package mods.extractor.pattern;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CausalPatternReader {

  static boolean                read = false;
  static List<ExtractorPattern> patterns;

  public static List<ExtractorPattern> getPatterns(String causeFilename, String effectFilename, PatternFactory pf) {
    if (!read) {
      readPatterns(causeFilename, effectFilename, pf);
    }
    return patterns;
  }

  private static void readPatterns(String causeFilename, String effectFilename, PatternFactory pf) {
    read = true;
    patterns = new ArrayList<ExtractorPattern>();
    try {
      ExtractorPatternReader reader = new ExtractorPatternReader(
          new FileReader(causeFilename), pf,"Agent");
      ExtractorPattern pattern = null;
      while ((pattern = reader.readPattern()) != null) {
        patterns.add(pattern);
      }
      reader.close();

      reader = new ExtractorPatternReader(
              new FileReader(effectFilename), pf,"Theme");
          pattern = null;
          while ((pattern = reader.readPattern()) != null) {
            patterns.add(pattern);
          }
          reader.close();
      }

     catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
