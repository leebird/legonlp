package mods.results;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;

import mods.utils.Env;
import mods.utils.Utils;
import mods.annotation.Event;
import mods.annotation.Event.EventType;

public class EventHtmlRandomByTrigger extends EventHtml {

  public static void main(String args[])
      throws IOException, BadLocationException {

    EventHtmlRandomByTrigger b = new EventHtmlRandomByTrigger();

    EventType types[] = new EventType[] { //
    EventType.Gene_expression,//
//        EventType.Phosphorylation, //
        EventType.Binding,//
//        EventType.Protein_catabolism, //
        EventType.Localization,//
        EventType.Transcription //
    };

    String fileprefix = Env.basedir + "/" + Env.DATA_SET;

    for (EventType type : types) {
      // fn
      b.type = type;
      String outputFilename;
      outputFilename = b.type + ".fn.byTrigger.random.html";
      System.out.println("create " + outputFilename);
      b.process(fileprefix + ".fn", outputFilename);

      // fp
      outputFilename = b.type + ".fp.byTrigger.random.html";
      System.out.println("create " + outputFilename);
      b.process(fileprefix + ".fp", outputFilename);
    }
  }

  List<Event>              fp;
  Map<String, List<Event>> map;
  EventType                type;

  public void process(String inputFilename, String outputFilename)
      throws IOException, BadLocationException {

    map = new HashMap<String, List<Event>>();

    ResultReader r = new ResultReader(new FileInputStream(inputFilename));
    fp = r.readResults();
    r.close();

    events = new ArrayList<Event>(fp);
    filteredEvents = new ArrayList<Event>();
    filterEvent(type);

    StringBuilder htmlText = new StringBuilder();
    for (int i = 0; i < filteredEvents.size() && i < 20; i++) {
      int index = (int) (Math.random() * filteredEvents.size());
      Event e = filteredEvents.remove(index);
      String text = Utils.readText(Env.DIR + "/" + e.filename + ".txt");
      htmlText.append(Event2Html.transform(e, text));
    }
    string2html(htmlText.toString(), outputFilename);
  }
}
