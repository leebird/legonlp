package mods.extractor.pattern;

public interface PatternFactory {
    
    public ExtractorPattern compile(String tregex, String name);
}
