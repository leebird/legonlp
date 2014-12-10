package mods.test;

import java.io.FileReader;
import java.io.IOException;

import mods.utils.Env;
import mods.extractor.pattern.BioNLPPatternFactory;
import mods.extractor.pattern.ExtractorPattern;
import mods.extractor.pattern.ExtractorPatternReader;

public class ExtractorPatternReaderTest {

  public static void main(String[] args)
      throws IOException {

      ExtractorPatternReader reader = new ExtractorPatternReader(new FileReader(Env.BASIC_PATTERN_TREGEX), BioNLPPatternFactory.instance(),Env.BASIC_PATTERN_TREGEX);

    ExtractorPattern pattern = null;
    while ((pattern = reader.readPattern()) != null) {
      System.out.println(pattern);
    }
    reader.close();
  }

}
