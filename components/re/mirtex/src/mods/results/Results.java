package mods.results;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;
import javax.swing.text.html.StyleSheet;

import mods.annotation.Event;
import mods.annotation.Event.EventType;

public abstract class Results {

  protected List<Event> events;
  protected List<Event> filteredEvents;
  static final String   dir = "data/2011/BioNLP-ST_2011_genia_train_data_rev1";

  public final void string2html(String htmlText, String outputFilename)
      throws IOException, BadLocationException {
    HTMLEditorKit kit = new HTMLEditorKit();
    HTMLDocument htmlDoc = (HTMLDocument) kit.createDefaultDocument();
    kit.read(new StringReader(htmlText), htmlDoc, 0);

    StyleSheet styleSheet = htmlDoc.getStyleSheet();
    styleSheet.addRule("p {margin-top:5px;margin-bottom:5px;text-indent:25px}");
    styleSheet.addRule("h1 {font-size:150%;font-weight:bold;}");
    styleSheet.addRule("h2 {font-size:100%;font-weight:bold;}");
    styleSheet.addRule(".T {color:blue; font-weight:bold;font-size:150%}");
    styleSheet.addRule(".A {color:green; font-weight:bold;font-size:150%}");
    styleSheet
        .addRule(".A1A2 {color:#DF01D7; font-weight:bold;font-size:150%}");
    styleSheet.addRule(".eventid {font-weight:bold;}");
    styleSheet.addRule(".table_event_type {width:200px;}");
    styleSheet.addRule(".table_event_trigger {width:200px;}");
    styleSheet.addRule(".table_primary {width:200px;}");
    styleSheet.addRule(".table_secondary {width:200px;}");
    styleSheet
        .addRule(".title {margin-top:10px;font-weight:bold; font-size:150%}");
    styleSheet
        .addRule("table {border-collapse:collapse; border:1px solid black;}");
    styleSheet.addRule("th,td {border:1px solid black;}");

    // print
    PrintStream out = new PrintStream(new FileOutputStream(outputFilename));

    StringWriter stringWriter = new StringWriter();
    HTMLWriter writer = new HTMLWriter(stringWriter, htmlDoc);
    writer.write();
    stringWriter.flush();

    out.println(stringWriter);
    out.close();
  }

  protected final void readResults(String filename)
      throws NumberFormatException, IOException {
    events = new ArrayList<Event>();
    ResultReader r = new ResultReader(new FileInputStream(filename));
    List<Event> es = r.readResults();
    events.addAll(es);
    r.close();
  }

  protected final void filterEvent(EventType type) {
    filteredEvents = new ArrayList<Event>();
    for (Event e : events) {
      if (e.type == type) {
        filteredEvents.add(e);
      }
    }
  }
}
