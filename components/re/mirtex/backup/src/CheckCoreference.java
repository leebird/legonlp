import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;
import javax.swing.text.html.StyleSheet;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.A2EventReader;
import pengyifan.bionlpst.v2.annotation.A3EventReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;
import pengyifan.bionlpst.v2.results.Event2Html;
import edu.stanford.nlp.util.Pair;

public class CheckCoreference extends BatchProcessor {

  public static void main(String args[]) {
    FileProcessor fileProcessor = new CheckCoreferenceFile();
    CheckCoreference batchProcessor = new CheckCoreference(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }

  List<Pair<Event, Event>> rep;

  public CheckCoreference(FileProcessor fileProcessor) {
    super(fileProcessor);
    rep = new ArrayList<Pair<Event, Event>>();
  }

  @Override
  public void processDir(String dir) {
    for (File f : allFiles(dir)) {
      String filename = f.getName();
      filename = filename.substring(0, filename.lastIndexOf('.'));
      fileProcessor.processFile(dir, filename);

      CheckCoreferenceFile p = (CheckCoreferenceFile) fileProcessor;
      rep.addAll(p.rep);

    }

    // print html
    StringBuilder htmlText = new StringBuilder();
    int sum = rep.size();
    System.err.println("sum = " + sum);

    for (Pair<Event, Event> p : rep) {
      Entity trigger = new Entity(p.first.trigger.id,
          (p.second.type + "").toLowerCase(), p.first.trigger.tokens);
      Entity argument1 = new Entity(p.first.argument.id, "pred",
          p.first.argument.tokens);
      Entity argument2 = new Entity(p.second.argument.id, "gold",
          p.second.argument.tokens);
      String text = Utils.readText(Env.DIR + "/" + p.first.filename + ".txt");
      htmlText.append(Event2Html.transform(
          p.first.filename,
          text,
          Arrays.asList(trigger),
          Arrays.asList(argument1, argument2)));
    }
    try {
      string2html(htmlText.toString(), Env.DATA_SET + ".co.html");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

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
}

class CheckCoreferenceFile extends FileProcessor {

  List<Event>              a2Events;
  List<Event>              a3Events;
  List<Pair<Event, Event>> rep;
  String                   text;

  @Override
  protected void readResource(String dir, String filename) {

    super.filename = filename;

    text = Utils.readText(dir + "/" + filename + ".txt");

    String a1filename = dir + "/" + filename + ".a1";
    String a2filename = dir + "/" + filename + ".a2";
    String a3filename = dir + "/" + filename + ".a3";

    List<Entity> ts = A1EntityReader.readEntities(a1filename);
    ts.addAll(A1EntityReader.readEntities(a2filename));
    a2Events = A2EventReader.readEvents(a1filename, a2filename, ts);

    a3Events = A3EventReader.readEvents(a3filename);

    // filter a2
    EventType types[] = { EventType.Activity, EventType.Gene_expression,
        EventType.Binding, EventType.Localization, EventType.Phosphorylation,
        EventType.Protein_catabolism, EventType.Transcription };
    List<Event> extractedEvents = new ArrayList<Event>();
    for (EventType t : types) {
      extractedEvents.addAll(A3EventReader.readEvents(a3filename + "." + t));
    }
    Iterator<Event> itr = a2Events.iterator();
    while (itr.hasNext()) {
      Event e2 = itr.next();
      boolean b = false;
      for (EventType t : types) {
        if (e2.type == t) {
          b = true;
          break;
        }
      }
      if (!b) {
        itr.remove();
        continue;
      }

      for (Event e3 : extractedEvents) {
        if (e2.trigger.from() == e3.trigger.from()
            && e2.trigger.to() == e3.trigger.to()
            && e2.argument.from() == e3.argument.from()
            && e2.argument.to() == e3.argument.to()) {
          itr.remove();
          break;
        }
      }
    }
  }

  @Override
  public void processFile(String dir, String filename) {

    System.out.println(filename);
    super.processFile(dir, filename);

    rep = new ArrayList<Pair<Event, Event>>();

    for (Event e3 : a3Events) {
      boolean b = false;
      // theme
      Pattern p = Pattern.compile("^(one|two|three|four|five|six|seven)");
      String argStr = text.substring(e3.argument.from(), e3.argument.to());
      // its
      if (e3.argument.tokens.size() == 1
          && e3.argument.getFirst().pos.startsWith("PRP")
          && !e3.argument.getFirst().word.equalsIgnoreCase("we")) {
        b = true;
      } else if (e3.argument.tokens.size() == 1
          && (e3.argument.getFirst().word.startsWith("this")
              || e3.argument.getFirst().word.startsWith("that")
              || e3.argument.getFirst().word.startsWith("these") //
          || e3.argument.getFirst().word.startsWith("those"))) {
        b = true;
      } else if (p.matcher(argStr).find()) {
        b = true;
      }

      if (b) {
        for (Event e2 : a2Events) {
          // trigger
          if (e2.trigger.from() == e3.trigger.from()
              && e2.trigger.to() == e3.trigger.to()) {
            rep.add(new Pair<Event, Event>(e3, e2));
          }
        }
      }
    }
  }
}