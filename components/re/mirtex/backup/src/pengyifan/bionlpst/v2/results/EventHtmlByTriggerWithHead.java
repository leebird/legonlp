//package pengyifan.bionlpst.v2.results;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import javax.swing.text.BadLocationException;
//
//import pengyifan.bionlpst.v2.Env;
//import pengyifan.bionlpst.v2.Utils;
//import pengyifan.bionlpst.v2.annotation.A1EntityReader;
//import pengyifan.bionlpst.v2.annotation.Entity;
//import pengyifan.bionlpst.v2.annotation.Event;
//import pengyifan.bionlpst.v2.annotation.Event.EventType;
//import edu.stanford.nlp.util.Pair;
//
//public class EventHtmlByTriggerWithHead extends EventHtml {
//
//  public static void main(String args[])
//      throws IOException, BadLocationException {
//
//    EventHtmlByTriggerWithHead b = new EventHtmlByTriggerWithHead();
//
//    EventType types[] = new EventType[] { //
//    // EventType.Gene_expression,//
//    // EventType.Phosphorylation, //
//    EventType.Binding,//
//    // EventType.Protein_catabolism, //
//    // EventType.Localization,//
//    // EventType.Transcription //
//    };
//
//    String fileprefix = Env.basedir + "/" + Env.DATA_SET;
//
//    for (EventType type : types) {
//      // fn
//      b.type = type;
//      String outputFilename;
//      outputFilename = b.type + ".fn.byTrigger.html";
//      System.out.println("create " + outputFilename);
//      b.process(fileprefix + ".fn", outputFilename);
//
//      // fp
//      outputFilename = b.type + ".fp.byTrigger.html";
//      System.out.println("create " + outputFilename);
//      b.process(fileprefix + ".fp", outputFilename);
//
//      // tp
//      outputFilename = b.type + ".tp.byTrigger.html";
//      System.out.println("create " + outputFilename);
//      b.process(fileprefix + ".tp", outputFilename);
//
//      // apptp
//      // outputFilename = b.type + ".apptp.byTrigger.html";
//      // System.out.println("create " + outputFilename);
//      // b.process2(fileprefix + ".apptp", outputFilename);
//    }
//  }
//
//  List<Event>              fp;
//  Map<String, List<Event>> map;
//  EventType                type;
//
//  public void process2(String inputFilename, String outputFilename)
//      throws IOException, BadLocationException {
//
//    Map<String, List<Pair<Event, Event>>> map2 = new HashMap<>();
//
//    ResultReader r = new ResultReader(new FileInputStream(inputFilename));
//    fp = r.readResults();
//    r.close();
//
//    List<Pair<Event, Event>> tps = new ArrayList<>();
//    for (int i = 0; i < fp.size(); i += 2) {
//      tps.add(new Pair<Event, Event>(fp.get(i), fp.get(i + 1)));
//    }
//
//    for (Pair<Event, Event> p : tps) {
//      // String text = Utils.readText(Env.DIR + "/" + e.filename + ".txt");
//
//      String triggerStr = p.first.trigger.tokens.get(0).word;
//
//      triggerStr = triggerStr.toLowerCase();
//      List<Pair<Event, Event>> value = map2.get(triggerStr);
//      if (value == null) {
//        value = new ArrayList<>();
//        map2.put(triggerStr, value);
//      }
//      value.add(p);
//    }
//    // sort
//    List<Entry<String, List<Pair<Event, Event>>>> entries = new ArrayList<>(
//        map2.entrySet());
//    Collections.sort(
//        entries,
//        new Comparator<Entry<String, List<Pair<Event, Event>>>>() {
//
//          @Override
//          public int compare(Entry<String, List<Pair<Event, Event>>> arg0,
//              Entry<String, List<Pair<Event, Event>>> arg1) {
//            return Integer.compare(arg1.getValue().size(), arg0.getValue()
//                .size());
//          }
//
//        });
//
//    StringBuilder htmlText = new StringBuilder();
//    for (Entry<String, List<Pair<Event, Event>>> e : entries) {
//      List<Pair<Event, Event>> value = e.getValue();
//      htmlText
//          .append(String.format("<h1>%s: %d</h1>", e.getKey(), value.size()));
//    }
//    for (Entry<String, List<Pair<Event, Event>>> e : entries) {
//      List<Pair<Event, Event>> value = e.getValue();
//      htmlText
//          .append(String.format("<h1>%s: %d</h1>", e.getKey(), value.size()));
//      for (Pair<Event, Event> p : value) {
//        Event t = p.first;
//        Event appt = p.second;
//        htmlText.append(transform(t, appt));
//      }
//    }
//    string2html(htmlText.toString(), outputFilename);
//  }
//
//  public String transform(Event tp, Event apptp)
//      throws IOException, BadLocationException {
//    // read text
//    String text = Utils.readText((Env.DIR) + "/" + tp.filename + ".txt");
//
//    StringBuilder headText = new StringBuilder();
//    // add filename
//    headText.append(String.format(
//        "<h2 id=\"%s\">%s</h2>",
//        tp.filename,
//        tp.filename));
//    headText.append("<p>");
//    for (int i = 0; i < text.length(); i++) {
//      String startTag = getStartTag2(apptp, i);
//      String endTag = getEndTag2(apptp, i);
//
//      String startTag2 = getStartTag(tp, i);
//      String endTag2 = getEndTag(tp, i);
//
//      char c = text.charAt(i);
//      headText.append(startTag);
//      headText.append(startTag2);
//      if (c == '\n') {
//        headText.append("</p><p>");
//      } else {
//        headText.append(c);
//      }
//      headText.append(endTag2);
//      headText.append(endTag);
//    }
//    return headText.toString();
//  }
//
//  protected String getStartTag2(Event e, int index) {
//    String startTag = "";
//    if (e.trigger.from() == index) {
//      startTag += "<span class=\"A1A2\">";
//    } else if (e.argument.from() == index) {
//      startTag += "<span class=\"A1A2\">";
//    }
//
//    return startTag;
//  }
//
//  protected String getEndTag2(Event e, int index) {
//    String endTag = "";
//
//    if (e.trigger.to() == index + 1 || e.argument.to() == index + 1) {
//      endTag += "</span>";
//    }
//    return endTag;
//  }
//
//  public void process(String inputFilename, String outputFilename)
//      throws IOException, BadLocationException {
//
//    map = new HashMap<>();
//
//    ResultReader r = new ResultReader(new FileInputStream(inputFilename));
//    fp = r.readResults();
//    r.close();
//
//    events = new ArrayList<>(fp);
//    filteredEvents = new ArrayList<>();
//    filterEvent(type);
//
//    for (Event e : filteredEvents) {
//      // String text = Utils.readText(Env.DIR + "/" + e.filename + ".txt");
//
//      String triggerStr = e.trigger.tokens.get(0).word;
//
//      triggerStr = triggerStr.toLowerCase();
//      List<Event> value = map.get(triggerStr);
//      if (value == null) {
//        value = new ArrayList<>();
//        map.put(triggerStr, value);
//      }
//      value.add(e);
//    }
//    // sort
//    List<Entry<String, List<Event>>> entries = new ArrayList<>(map.entrySet());
//    Collections.sort(entries, new Comparator<Entry<String, List<Event>>>() {
//
//      @Override
//      public int compare(Entry<String, List<Event>> arg0,
//          Entry<String, List<Event>> arg1) {
//        return Integer.compare(arg1.getValue().size(), arg0.getValue().size());
//      }
//
//    });
//
//    StringBuilder htmlText = new StringBuilder();
//    for (Entry<String, List<Event>> e : entries) {
//      List<Event> value = e.getValue();
//      htmlText
//          .append(String.format("<h1>%s: %d</h1>", e.getKey(), value.size()));
//      for (int i = 0; i < value.size(); i++) {
//        Event event = value.get(i);
//
//        List<Entity> a1ts = A1EntityReader.readEntities(Env.DIR
//            + "/"
//            + event.filename
//            + ".a1");
//        boolean found = false;
//        for (Entity a1t : a1ts) {
//          if (event.argument.from() <= a1t.from()
//              && a1t.to() <= event.argument.to()) {
//            found = true;
//            htmlText.append(transform(new Event(event.filename, event.type,
//                event.trigger, a1t, event.comment), event));
//            break;
//          }
//        }
//        if (!found) {
//          htmlText.append(transform(event));
//        }
//      }
//    }
//    string2html(htmlText.toString(), outputFilename);
//  }
//}
