package mods.test;

import java.io.IOException;
import java.util.List;

import mods.utils.Env;
import mods.annotation.A1EntityReader;
import mods.annotation.CoEventReader;
import mods.annotation.Entity;
import mods.annotation.Event;

public class A2EventReaderTest {

  public static void main(String[] args)
      throws IOException {
//    List<Entity> ts = A1EntityReader.readEntities(Env.DIR
//        + "/PMC-1447668-09-Results-07.a1");
//    ts.addAll(A1EntityReader.readEntities(Env.DIR
//        + "/PMC-1447668-09-Results-07.a2"));
//
//    A2EventReader reader = new A2EventReader(Env.DIR
//        + "/PMC-1447668-09-Results-07.a1", Env.DIR
//        + "/PMC-1447668-09-Results-07.a2", ts);
//    List<Event> events = reader.readEvents();
//    for (Event e : events) {
//      System.out.println(e);
//    }
//    reader.close();
    
    String cofilename = Env.DIR + "/BioNLP-ST_2011_coreference_training_data/PMID-10068671.a2";
    List<Entity> ts = A1EntityReader.readEntities(cofilename);
    List<Event> events = CoEventReader.readEvents(cofilename, ts);
    for (Event e : events) {
      System.out.println(e);
    }
  }
}
