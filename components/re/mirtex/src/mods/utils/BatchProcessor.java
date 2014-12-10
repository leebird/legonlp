package mods.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class BatchProcessor {

  protected String        dir;
  protected FileProcessor fileProcessor;
  
  public BatchProcessor(FileProcessor fileProcessor) {
    this.fileProcessor = fileProcessor;
  }

  public void processDir(String dir) {
    for (File f : allFiles(dir)) {
      String filename = f.getName();
      filename = filename.substring(0, filename.lastIndexOf('.'));
      fileProcessor.processFile(dir, filename);
    }
  }

  public File[] allFiles(String dir) {
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
