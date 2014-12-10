package pengyifan.bionlpst.bmc;

import java.io.IOException;

import pengyifan.bionlpst.v2.BatchProcessor;
import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import pengyifan.bionlpst.v2.eval.Merge2A4;
import pengyifan.bionlpst.v2.filter.ActivityFilter;
import pengyifan.bionlpst.v2.filter.BindingFilter;
import pengyifan.bionlpst.v2.filter.CatabolismFilter;
import pengyifan.bionlpst.v2.filter.EventFilter;
import pengyifan.bionlpst.v2.filter.GeneExpressionFilter;
import pengyifan.bionlpst.v2.filter.LocalizationFilter;
import pengyifan.bionlpst.v2.filter.PhosphorylationFilter;
import pengyifan.bionlpst.v2.filter.TranscriptionFilter;
import pengyifan.bionlpst.v2.filter.RegulationFilter;
import pengyifan.bionlpst.v2.ptb.PtbAligment;
import pengyifan.bionlpst.v3.extractor.Extractor;
import pengyifan.bionlpst.v3.extractor.RefExtractor;
import pengyifan.bionlpst.v3.link.LinkArgument;
import pengyifan.bionlpst.v3.simp.GenerateParCooSimplification;
import pengyifan.bionlpst.v3.simp.GenerateSimplification;
import pengyifan.bionlpst.parser.CharniakParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BatchProcessorBmcTest {

  public static void main(String args[])
      throws IOException {

    if (args.length != 1) {
      System.err.println("no FileProcessor");
      System.exit(1);
    }
    
    String className = args[0];
    
    // added by leebird
    List<String> steps = new ArrayList<String>();
    
    // steps to go through
    //steps.add("GenerateSimplification");
    // steps.add("GenerateParCooSimplification");
    //steps.add("Extractor");

    //steps.add("BindingFilter");
    
    //steps.add("ActivityFilter");
    //steps.add("GeneExpressionFilter");
    //steps.add("TranscriptionFilter");
    //steps.add("LocalizationFilter");
    //steps.add("CatabolismFilter");
    //steps.add("PhosphorylationFilter");
    // steps.add("RegulationFilter");
    //steps.add("Merge2A4");
    //steps.add("EventFilter");
    steps.add("CharniakParser");

    
    
    for (String step : steps) {
    	className = step;
    	System.err.println(step);
    // end
    
    FileProcessor fileProcessor = null;
    if (className.equals("LinkArgument")) {
      fileProcessor = new LinkArgument(true, true, false);
    } else if (className.equals("GenerateSimplification")) {
      fileProcessor = new GenerateSimplification();
    } else if (className.equals("GenerateParCooSimplification")) {
      fileProcessor = new GenerateParCooSimplification();
    } else if (className.equals("PtbAligment")) {
      fileProcessor = new PtbAligment();
    } else if (className.equals("BindingFilter")) {
      fileProcessor = new BindingFilter();
    } else if (className.equals("ActivityFilter")) {
      fileProcessor = new ActivityFilter();
    } else if (className.equals("GeneExpressionFilter")) {
      fileProcessor = new GeneExpressionFilter();
    } else if (className.equals("TranscriptionFilter")) {
      fileProcessor = new TranscriptionFilter();
    } else if (className.equals("LocalizationFilter")) {
      fileProcessor = new LocalizationFilter();
    } else if (className.equals("CatabolismFilter")) {
      fileProcessor = new CatabolismFilter();
    } else if (className.equals("PhosphorylationFilter")) {
      fileProcessor = new PhosphorylationFilter();
    } else if (className.equals("Merge2A4")) {
      fileProcessor = new Merge2A4();
    } else if (className.equals("EventFilter")) {
      fileProcessor = new EventFilter();
    } else if (className.equals("Extractor")) {
      fileProcessor = new Extractor(true, Extractor.ALL);
    } else if (className.equals("RefExtractor")) {
      fileProcessor = new RefExtractor();
    } else if (className.equals("RegulationFilter")) {
      fileProcessor = new RegulationFilter();
    } else if (className.equals("CharniakParser")) {
      fileProcessor = new CharniakParser();
    } else {
      System.err.println("no FileProcessor: " + className);
      System.exit(1);
    }
    BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
    batchProcessor.processDir(Env.DIR);
    }  
  }
}
