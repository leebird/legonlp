package mods.test;

import mods.utils.BatchProcessor;
import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.link.LinkArgument;

public class LinkArgumentBatch {

  public static void main(String args[]) {
    FileProcessor fileProcessor = new LinkArgument(true, true, false);
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }
}
