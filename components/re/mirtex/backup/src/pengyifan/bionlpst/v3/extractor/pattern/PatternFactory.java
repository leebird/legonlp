package pengyifan.bionlpst.v3.extractor.pattern;

public interface PatternFactory {

  public ExtractorPattern compile(String tregex, String name);
}
