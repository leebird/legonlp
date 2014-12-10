package mods.annotation;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;

public class CompositeEvent extends Event {


  final public List<Entity>       argumentList;
  public String	directness;
  public int direction;
  public boolean translation;

  public CompositeEvent(String filename, EventType type, Entity trigger,
      ArgumentType argumentType, List<Entity> argument, String comment) {
    super(filename,type,trigger,argumentType,argument.get(0),comment);

    this.argumentList = argument;
    this.directness = "unknown";
    this.translation = false;
  }

  @Override
  public String toString() {
    return String.format(
        "E\t%s\tTrigger:%s\t%s:%s\t%s",
          type,
          trigger.tokens,
          argumentType,
          argument,
          comment);
  }

  /**
   * remove e from list
   * 
   * @return number of elements removed
   */
  public static void remove(List<CompositeEvent> list, CompositeEvent e) {
    list.remove(e);
  }

  /**
   * find the first element in list, who equals to e
   * 
   * @return the first event in list
   */
  public static CompositeEvent find(List<CompositeEvent> list, CompositeEvent e) {
    return CollectionUtils.find(list, PredicateUtils.equalPredicate(e));
  }

  @Override
  public boolean equals(Object obj) {
    CompositeEvent e = (CompositeEvent) obj;
    return e.type == type
        && e.trigger.equals(trigger)
          && e.argument.equals(argument)
          	&& e.argumentType == argumentType;
  }


}
