package mods.ptb;

import org.apache.commons.lang3.Range;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.LabelFactory;
import edu.stanford.nlp.ling.StringLabel;

/**
 * [beginPosition, endPosition)
 * 
 * @author Yifan Peng
 * @version Sep 22, 2013
 * 
 */
@SuppressWarnings("serial")
public class OffsetLabel extends StringLabel {

  public OffsetLabel() {
    super();
  }

  public OffsetLabel(Label label) {
    super(label);
  }

  public OffsetLabel(String str, int beginPosition, int endPosition) {
    super(str, beginPosition, endPosition);
  }

  public OffsetLabel(String str) {
    super(str);
  }

  public Range<Integer> range() {
    if (beginPosition() == -1) {
      System.err.println("Not a leaf label: " + this);
    }
    return Range.between(beginPosition(), endPosition());
  }

  @Override
  public LabelFactory labelFactory() {
    return OffsetLabelFactory.instance();
  }

  @Override
  public String toString() {
    if (beginPosition() != -1) {
      return super.value() + "_" + beginPosition() + "_" + endPosition();
    } else {
      return super.value();
    }
  }
}
