package pengyifan.bionlpst.v2.results;

import java.io.IOException;
import java.util.List;

import javax.swing.text.BadLocationException;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.Utils;
import pengyifan.bionlpst.v2.annotation.A1EntityReader;
import pengyifan.bionlpst.v2.annotation.Entity;
import pengyifan.bionlpst.v2.annotation.Event;
import pengyifan.bionlpst.v2.annotation.Event.EventType;

public class EventHtml extends Results {

  EventType type;
  List<Entity> a1ts, a2ts;

  /**
   * @param args
   * @throws IOException
   * @throws BadLocationException
   */
  public static void main(String[] args)
      throws IOException, BadLocationException {

    EventHtml html = new EventHtml();
    String fileprefix = Env.basedir + "/" + Env.DATA_SET;

    for (int i = 0; i < EventType.values().length - 3; i++) {
      html.type = EventType.values()[i];
      html.print(fileprefix + ".tp", fileprefix + "." + html.type + ".tp.html");
      html.print(fileprefix + ".fp", fileprefix + "." + html.type + ".fp.html");
      html.print(fileprefix + ".fn", fileprefix + "." + html.type + ".fn.html");

      html.printRandom(
          Env.DIR + "/../BioNLP-ST_2011_genia_train_data_rev1.fp",
          (Env.DIR) + "/../" + html.type + ".fp.random.html");
    }

  }

  public void printRandom(String inputFilename, String outputFilename)
      throws NumberFormatException, IOException, BadLocationException {
    // read tp, fp, or fn
    readResults(inputFilename);
    // find first 15
    filterEvent(type);
    // process event
    StringBuilder htmlText = new StringBuilder();
    String filename = null;
    for (int j = 0; j < 30 && !filteredEvents.isEmpty(); j++) {
      int index = (int) (Math.random() * filteredEvents.size());
      Event e = filteredEvents.remove(index);
      if (!e.filename.equals(filename)) {
        a1ts = A1EntityReader.readEntities(Env.DIR + "/" + e.filename + ".a1");
        a2ts = A1EntityReader.readEntities(Env.DIR + "/" + e.filename + ".a2");
        filename = e.filename;
      }
      switch (type) {
      case Phosphorylation:
      case Protein_catabolism:
        break;
      case Gene_expression:
        break;
      default:
        break;
      }
      String text = Utils.readText(Env.DIR + "/" + e.filename + ".txt");
      htmlText.append(Event2Html.transform(e, text));
    }
    string2html(htmlText.toString(), outputFilename);
  }

  public void print(String inputFilename, String outputFilename)
      throws NumberFormatException, IOException, BadLocationException {
    // read tp, fp, or fn
    readResults(inputFilename);
    // find first 15
    filterEvent(type);
    // process event
    StringBuilder htmlText = new StringBuilder();

    String filename = null;
    for (Event e : filteredEvents) {
      if (!e.filename.equals(filename)) {
        a1ts = A1EntityReader.readEntities(Env.DIR + "/" + e.filename + ".a1");
        a2ts = A1EntityReader.readEntities(Env.DIR + "/" + e.filename + ".a2");
        filename = e.filename;
      }
      String text = Utils.readText(Env.DIR + "/" + e.filename + ".txt");
      htmlText.append(Event2Html.transform(e, text));
    }
    string2html(htmlText.toString(), outputFilename);
  }
}