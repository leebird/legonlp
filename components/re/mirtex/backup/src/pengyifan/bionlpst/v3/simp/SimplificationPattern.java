package pengyifan.bionlpst.v3.simp;

import java.util.List;

import pengyifan.bionlpst.v2.ptb.TOperationPattern;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class SimplificationPattern {

  private final TregexPattern           tregexPattern;
  private final List<TOperationPattern> operations;

  public SimplificationPattern(
      TregexPattern tregexPattern,
      List<TOperationPattern> operations) {
    this.tregexPattern = tregexPattern;
    this.operations = operations;
  }
  
  public final TregexPattern getTregexPattern() {
    return tregexPattern;
  }
  
  public final List<TOperationPattern> getOperations() {
    return operations;
  }
  
  @Override
  public String toString() {
    return "[pattern=" + tregexPattern + ",operations=" + operations + "]";
  }
}
