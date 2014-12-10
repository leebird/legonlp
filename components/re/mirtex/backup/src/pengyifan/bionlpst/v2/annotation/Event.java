package pengyifan.bionlpst.v2.annotation;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;

public class Event implements Comparable<Event> {

  public static enum EventType {
    Gene_expression, Transcription, Protein_catabolism, Phosphorylation,
    Localization, Binding, Regulation, Positive_regulation,
    Negative_regulation, PartWhole, MemberCollection, Event, Coref, Activity, /*
                                                                               * part
                                                                               * ,
                                                                               * member
                                                                               * ,
                                                                               */
    /* referer, entity1, */MiRNATarget;

    public static EventType getEnum(String str) {
      if (str.equals("part")) {
        return PartWhole;
      } else if (str.equals("member")) {
        return MemberCollection;
      } else if (str.equals("referer")) {
        return Coref;
      } else if (str.equals("Target")) {
        return MiRNATarget;
      } else {
        return valueOf(str);
      }
    }
  }

  public static enum TriggerType {

  }

  public static enum ArgumentType {
    Theme, Agent, Coreference, Site, CSite, ToLoc, AtLoc, General;

    public static ArgumentType getEnum(String str) {
      if (str.equals("theme")) {
        return Theme;
      } else if (str.equals("agent")) {
        return Agent;
      } else if (str.equalsIgnoreCase("cause")) {
        return Agent;
      } else if (str.startsWith("Theme")) {
        return Theme;
      } else if (str.startsWith("Site")) {
        return Site;
      } else {
        return valueOf("General");
      }
    }
  }

  final public String       filename;
  final public EventType    type;
  final public Entity       trigger;
  final public ArgumentType argumentType;
  final public Entity       argument;
  final public String       comment;

  public Event(String filename, EventType type, Entity trigger,
      ArgumentType argumentType, Entity argument, String comment) {
    this.filename = filename;
    this.type = type;
    this.trigger = trigger;
    this.argumentType = argumentType;
    this.argument = argument;
    this.comment = comment;
  }

  public Event(Event e) {
    this(e.filename, e.type, e.trigger, e.argumentType, e.argument, e.comment);
  }

  @Override
  public String toString() {
    return String.format(
        "E\t%s\tTrigger:%s\t%s:%s\t%s",
          type,
          trigger.tokens,
          argumentType,
          argument.tokens,
          comment);
  }

  /**
   * remove e from list
   * 
   * @return number of elements removed
   */
  public static void remove(List<Event> list, Event e) {
    list.remove(e);
  }

  /**
   * find the first element in list, who equals to e
   * 
   * @return the first event in list
   */
  public static Event find(List<Event> list, Event e) {
    return Collectionmods.Utils.find(list, Predicatemods.Utils.equalPredicate(e));
  }

  @Override
  public boolean equals(Object obj) {
    Event e = (Event) obj;
    return e.type == type
        && e.trigger.equals(trigger)
          && e.argument.equals(argument);
  }

  @Override
  public int compareTo(Event e) {
    int i = trigger.compareTo(e.trigger);
    if (i == 0) {
      i = argument.compareTo(e.argument);
    }
    return i;
  }
}
