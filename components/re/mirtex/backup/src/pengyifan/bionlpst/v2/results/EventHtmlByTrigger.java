package pengyifan.bionlpst.v2.results;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.BadLocationException;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;

public class EventHtmlByTrigger extends EventHtml {

  public static void main(String args[])
      throws IOException, BadLocationException {

    EventHtmlByTrigger b = new EventHtmlByTrigger();

    EventType types[] = new EventType[] { //
    EventType.Gene_expression,//
    // EventType.Phosphorylation, //
    // EventType.Binding,//
    // EventType.Protein_catabolism, //
    // EventType.Localization,//
    // EventType.Transcription //
    };

    String fileprefix = Env.basedir + "/" + Env.DATA_SET;

    for (EventType type : types) {
      b.type = type;
      String prefix = Env.DATA_SET + "." + b.type;
      String outputFilename;

      // // fn
      // outputFilename = prefix + ".fn.byTrigger.html";
      // System.out.println("create " + outputFilename);
      // b.process(fileprefix + ".fn", outputFilename);
      //
      // // fp
      // outputFilename = prefix + ".fp.byTrigger.html";
      // System.out.println("create " + outputFilename);
      // b.process(fileprefix + ".fp", outputFilename);

      // tp
      outputFilename = prefix + ".tp.byTrigger.html";
      System.out.println("create " + outputFilename);
      b.process(fileprefix + ".tp", outputFilename);

    }
    // b.process("dev-trans-all.fn", "dev-trans-all.html");

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

    for (Event e : filteredEvents) {
      String text = Utils.readText(Env.DIR + "/" + e.filename + ".txt");
      String triggerStr = text.substring(e.trigger.from(), e.trigger.to());

      triggerStr = triggerStr.toLowerCase();
      List<Event> value = map.get(triggerStr);
      if (value == null) {
        value = new ArrayList<Event>();
        map.put(triggerStr, value);
      }
      value.add(e);
    }
    // sort
    List<Entry<String, List<Event>>> entries = new ArrayList<Entry<String, List<Event>>>(
        map.entrySet());
    Collections.sort(entries, new Comparator<Entry<String, List<Event>>>() {

      @Override
      public int compare(Entry<String, List<Event>> arg0,
          Entry<String, List<Event>> arg1) {
        if (arg1.getValue().size() < arg0.getValue().size()) {
          return -1;
        } else if (arg1.getValue().size() == arg0.getValue().size()) {
          return 0;
        } else {
          return 1;
        }
      }

    });

    StringBuilder htmlText = new StringBuilder();
    int sum = 0;

    for (Entry<String, List<Event>> e : entries) {
      List<Event> value = e.getValue();
      htmlText.append(String.format("<p>%s: %d</p>", e.getKey(), value.size()));
      sum += value.size();
    }
    System.err.println("sum = " + sum);

    for (Entry<String, List<Event>> e : entries) {
      List<Event> value = e.getValue();
      htmlText
          .append(String.format("<h1>%s: %d</h1>", e.getKey(), value.size()));
      for (int i = 0; i < value.size(); i++) {
        Event event = value.get(i);
        String text = Utils.readText(Env.DIR + "/" + event.filename + ".txt");
        htmlText.append(Event2Html.transform(event, text));
      }
    }
    string2html(htmlText.toString(), outputFilename);
  }
}
