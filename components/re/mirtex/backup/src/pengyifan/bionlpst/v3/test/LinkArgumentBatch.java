package pengyifan.bionlpst.v3.test;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v3.link.LinkArgument;

public class LinkArgumentBatch {

  public static void main(String args[]) {
    FileProcessor fileProcessor = new LinkArgument(true, true, false);
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
  }
}
