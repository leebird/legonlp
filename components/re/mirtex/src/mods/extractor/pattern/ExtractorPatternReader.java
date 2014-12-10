package mods.extractor.pattern;

import java.io.Closeable;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class ExtractorPatternReader implements Closeable {

  LineNumberReader reader;
  PatternFactory   pf;
    String argumentType;

    public ExtractorPatternReader(Reader reader, PatternFactory pf,String argumentType) {
      this.reader = new LineNumberReader(reader);
      this.pf = pf;
      this.argumentType = argumentType;
  }
    
  public ExtractorPattern readPattern()
      throws IOException {
      
      String line;
      
      String name = null;
      
      ExtractorPattern pattern = null;
      
    while ((line = reader.readLine()) != null) {
        
        line = line.trim();

        if (line.isEmpty()) {
            continue;
        }

        if (line.startsWith("#") || line.startsWith("//")) {
            continue;
        }
        
        int colon = line.indexOf(':');
        assert colon != -1 : line;

        String first = line.substring(0, colon).trim();

        String second = line.substring(colon + 1).trim();
        
        if (first.equals("tregex")) {
            pattern = pf.compile(second, name);
            
        } else if (first.equals("name")) {
            name = second;
            
        } else {
            throw new IllegalArgumentException("cannot parse line["
                                               + reader.getLineNumber()
                                               + "]: "
                                               + line);
        }
      if (pattern != null) {
    	  pattern.setArgumentType(this.argumentType);
          break;
      }
    }
        
    return pattern;
  }

  @Override
  public void close()
      throws IOException {
    reader.close();
  }

}
