package pengyifan.bionlpst.v2.ptb;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;

public class OffsetLabelFactory implements LabelFactory {

  private static OffsetLabelFactory instance = null;

  private OffsetLabelFactory() {

  }

  public static OffsetLabelFactory instance() {
    if (instance == null) {
      instance = new OffsetLabelFactory();
    }
    return instance;
  }

  /**
   * Create a new StringLabel with the given content.
   * 
   * @param labelStr
   * @param beginPosition Start offset in original text (inclusive)
   * @param endPosition End offset in original text (exclusive)
   * @return
   */
  public Label newLabel(String labelStr, int beginPosition, int endPosition) {
    return new OffsetLabel(labelStr, beginPosition, endPosition);
  }

  @Override
  public Label newLabel(String labelStr) {
    return new OffsetLabel(labelStr);
  }

  @Override
  public Label newLabel(String labelStr, int options) {
    return new OffsetLabel(labelStr);
  }

  @Override
  public Label newLabelFromString(String encodedLabelStr) {
    return new OffsetLabel(encodedLabelStr);
  }

  @Override
  public Label newLabel(Label oldLabel) {
    return new OffsetLabel(oldLabel);
  }
}
