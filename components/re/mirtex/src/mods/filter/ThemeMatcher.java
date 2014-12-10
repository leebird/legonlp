package mods.filter;

import java.util.LinkedList;
import java.util.List;

import mods.annotation.Entity;
import mods.annotation.Event;
import mods.annotation.Token;

public abstract class ThemeMatcher {

  protected List<Entity> a1ts;
  protected Entity       e;
  protected List<Entity> themes;
  protected boolean      isRefLink;

  public ThemeMatcher(List<Entity> a1ts, boolean isRefLink) {
    this.a1ts = a1ts;
    themes = new LinkedList<Entity>();
    this.isRefLink = isRefLink;
  }

  public abstract boolean matches(Event event, Entity e);

  public boolean lastTwoWord(String word_2, String word_1) {
    if (!isRefLink) {
      return false;
    }
    if (word_2.startsWith("protein") && word_1.startsWith("complex")) {
      return true;
    }
    if (word_2.startsWith("transcription") && word_1.startsWith("factor")) {
      return true;
    }
    return false;
  }

  public int getInIndex(List<Token> tokens) {
    for (int i = 1; i < tokens.size(); i++) {
      if (tokens.get(i).pos.equals("IN")) {
        return i;
      }
      if (tokens.get(i).pos.equals("VBG")) {
        return i;
      }
      if (tokens.get(i).pos.equals("RB")) {
        return i;
      }
    }
    return -1;
  }

  public List<Entity> getThemes() {
    return themes;
  }

  public Entity endsWith(Token t) {
    for (Entity a1t : a1ts) {
      if (t.to() == a1t.to()) {
        return a1t;
      }
    }
    return null;
  }
}
