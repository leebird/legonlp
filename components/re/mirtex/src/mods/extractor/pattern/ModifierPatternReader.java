package mods.extractor.pattern;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModifierPatternReader {

  static boolean                read = false;
  static List<ExtractorPattern> patterns;

  public static List<ExtractorPattern> getPatterns(String modifierFilename, PatternFactory pf) {
    if (!read) {
      readPatterns(modifierFilename, pf);
    }
    return patterns;
  }

  private static void readPatterns(String modifierFilename, PatternFactory pf) {
    read = true;
    patterns = new ArrayList<ExtractorPattern>();
    try {
      ExtractorPatternReader reader = new ExtractorPatternReader(
          new FileReader(modifierFilename), pf,"General");
      ExtractorPattern pattern = null;
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
