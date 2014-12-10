package mods.test;

import mods.utils.BatchProcessor;
import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.ptb.PtbAligment;

public class PtbAligmentBatch {

  public static void main(String args[]) {
    FileProcessor fileProcessor = new PtbAligment();
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }
}
