package pengyifan.bionlpst.v3.test;

import java.io.FileReader;
import java.io.IOException;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v3.extractor.pattern.BioNLPPatternFactory;
import pengyifan.bionlpst.v3.extractor.pattern.ExtractorPattern;
import pengyifan.bionlpst.v3.extractor.pattern.ExtractorPatternReader;

public class ExtractorPatternReaderTest {

  public static void main(String[] args)
      throws IOException {

    ExtractorPatternReader reader = new ExtractorPatternReader(new FileReader(
        Env.BASIC_PATTERN_TREGEX), BioNLPPatternFactory.instance());

    ExtractorPattern pattern = null;
    while ((pattern = reader.readPattern()) != null) {
      System.out.println(pattern);
    }
    reader.close();
  }

}
