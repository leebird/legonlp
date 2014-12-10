package pengyifan.bionlpst.v2.results;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.text.BadLocationException;

import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;

public class Event2Html {

  public static String transform(String filename, String text,
      List<Entity> triggers, List<Entity> arguments) {
    StringBuilder headText = new StringBuilder();
    // add filename
    headText.append(String.format("<h2 id=\"%s\">%s</h2>", filename, filename));
    headText.append("<p>");
    for (int i = 0; i < text.length(); i++) {
      String startTag = "";
      String endTag = "";

      for (Entity t : triggers) {
        startTag += getStartTag(t, i, "T");
        endTag += getEndTag(t, i);
      }

      for (Entity t : arguments) {
        startTag += getStartTag(t, i, "A");
        endTag += getEndTag(t, i);
      }

      char c = text.charAt(i);
      headText.append(startTag);
      if (c == '\n') {
        headText.append("</p><p>");
      } else {
        headText.append(c);
      }
      headText.append(endTag);
    }
    return headText.toString();
  }

  public static String transform(Event e, String text)
      throws IOException, BadLocationException {

    return transform(
        e.filename,
        text,
        Collections.singletonList(e.trigger),
        Collections.singletonList(e.argument));
    //
    // // read text
    // String text = Utils.readText((Env.DIR) + "/" + e.filename + ".txt");
    //
    // StringBuilder headText = new StringBuilder();
    // // add filename
    // headText.append(String.format(
    // "<h2 id=\"%s\">%s</h2>",
    // e.filename,
    // e.filename));
    // headText.append("<p>");
    // for (int i = 0; i < text.length(); i++) {
    // String startTag = getStartTag(e.trigger, i, "T");
    // startTag += getStartTag(e.argument, i, "A");
    //
    // String endTag = getEndTag(e.trigger, i);
    // char c = text.charAt(i);
    // headText.append(startTag);
    // if (c == '\n') {
    // headText.append("</p><p>");
    // } else {
    // headText.append(c);
    // }
    // headText.append(endTag);
    // }
    // return headText.toString();
  }

  protected static String getStartTag(Entity e, int index, String tag) {
    String startTag = "";
    if (e.from() == index) {
      startTag += "<span class=\"" + tag + "\">";
    }
    return startTag;
  }

  protected static String getEndTag(Entity e, int index) {
    String endTag = "";

    if (e.to() == index + 1) {
      endTag += "<sub>" + e.type + "</sub></span>";
    }
    return endTag;
  }
}
