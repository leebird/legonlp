package mods.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import mods.utils.Env;
import mods.utils.Utils;
import mods.annotation.A1EntityReader;
import mods.annotation.Entity;

public class BioNexTest {

  @Test
  public void test() {
    String dir = Env.basedir + "/BioNLP-ST_2011_genia_train_data_rev1";
    for (File f : allFiles(dir)) {
      String filename = f.getName();
      // System.out.println("test " + filename);
      filename = filename.substring(0, filename.lastIndexOf('.'));
      processFile(dir, filename);
    }
  }

  private void processFile(String dir, String filename) {
    // text
    String text = Utils.readText(dir + "/" + filename + ".txt");
    // bionex
    String a6filename = dir + "/bionex/" + filename + ".a2";

    List<Entity> entities = new ArrayList<Entity>();
    try {
      entities = A1EntityReader.readEntities(a6filename);
    } catch (IllegalArgumentException e) {
      System.err.println(a6filename);
      System.err.println(e.getMessage() + "\n");
    } catch (IllegalStateException e) {
      System.err.println(a6filename);
      System.err.println(e.getMessage() + "\n");
    }

    for (Entity e : entities) {
      try {
        assertEquals(e.getFirst().word, text.substring(e.from(), e.to()));
        // System.out.println(filename + ": " + e + " - passed");
      } catch (AssertionError error) {
        System.err.println(filename + ": " + e + " - failed");
      }
    }
  }

  private File[] allFiles(String dir) {
    File dirFile = new File(dir);
    File[] children = dirFile.listFiles(new FileFilter() {

      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".txt");
      }

    });

    Arrays.sort(children, new Comparator<File>() {

      @Override
      public int compare(File f0, File f1) {
        return f0.getName().compareTo(f1.getName());
      }

    });
    return children;
  }
}
