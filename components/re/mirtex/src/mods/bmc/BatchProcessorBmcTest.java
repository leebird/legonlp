package mods.bmc;

import java.io.IOException;

import mods.utils.BatchProcessor;
import mods.utils.Env;
import mods.utils.FileProcessor;
import mods.eval.Merge2A4;
import mods.filter.ActivityFilter;
import mods.filter.BindingFilter;
import mods.filter.CatabolismFilter;
import mods.filter.EventFilter;
import mods.filter.GeneExpressionFilter;
import mods.filter.LocalizationFilter;
import mods.filter.PhosphorylationFilter;
import mods.filter.TranscriptionFilter;
import mods.filter.RegulationFilter;
import mods.filter.GeneralFilter;
import mods.ptb.PtbAligment;
import mods.extractor.Extractor;
import mods.extractor.RefExtractor;
import mods.extractor.CausalExtractor;
import mods.link.LinkArgument;
import mods.simp.GenerateParCooSimplification;
import mods.simp.GenerateSimplification;
import mods.parser.Charniak;
import mods.pre.MiRNARecognizer;
import mods.pre.ReplaceEntity;
import mods.pre.RecoverEntity;
import mods.pos.ResoluteAnaphora;
import mods.pos.ExtractTarget;
import mods.pos.ExtractMiRNA;
import mods.pos.LinkCausal;
import mods.extractor.ModifierExtractor;

import java.util.ArrayList;
import java.util.List;

public class BatchProcessorBmcTest {

    public static void main(String args[])
        throws IOException {

        if (args.length < 2) {
            System.err.println("no FileProcessor or base dir");
            System.exit(1);
        }
    
        String eventType = args[0];
        String className = "";

        String basedir = args[1];
        Env.setBaseDir(basedir);

        // added by leebird
        List<String> steps = new ArrayList<String>();
    
        // pre-process
        //steps.add("MiRNARecognizer");
        //steps.add("ReplaceEntity");
        //steps.add("CharniakParser");        
        steps.add("PtbAligment");

        steps.add("GenerateSimplification");
        //steps.add("GenerateParCooSimplification");    

        // extract all relations
        steps.add("Extractor");
        steps.add("RefExtractor");
        steps.add("ModifierExtractor");        
        steps.add("CausalExtractor");

        // linking arguments
        steps.add("LinkArgument");
    
        // filters
        //steps.add("BindingFilter");    
        //steps.add("ActivityFilter");
        //steps.add("GeneExpressionFilter");
        //steps.add("TranscriptionFilter");
        //steps.add("LocalizationFilter");
        //steps.add("CatabolismFilter");
        //steps.add("PhosphorylationFilter");
        //steps.add("RegulationFilter");
        //steps.add("GeneralFilter");
        steps.add("GeneralFilterTarget");
        steps.add("GeneralFilterRegulate");

        // merge filter results
        //steps.add("Merge2A4");
        //steps.add("EventFilter");

        // extract results for miRNA
        

        steps.add("LinkCausal");
        steps.add("ResoluteAnaphoraTarget");
        steps.add("ExtractTarget");
        steps.add("ResoluteAnaphoraMiRNA");
        steps.add("ExtractMiRNA");
        //steps.add("RecoverEntity");
    
        for (String step : steps) {
            className = step;
            //System.err.println(step);
    
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
                fileProcessor = new Extractor(true, Extractor.ALL_SPLIT);
            } else if (className.equals("RefExtractor")) {
                fileProcessor = new RefExtractor();
            } else if (className.equals("CausalExtractor")) {
                fileProcessor = new CausalExtractor();
            } else if (className.equals("RegulationFilter")) {
                fileProcessor = new RegulationFilter();
            } else if (className.equals("CharniakParser")) {
                fileProcessor = new Charniak();
            } else if (className.equals("GeneralFilterTarget")) {
                fileProcessor = new GeneralFilter("MiRNATarget");
            } else if (className.equals("GeneralFilterRegulate")) {
                fileProcessor = new GeneralFilter("Regulation");
            } else if (className.equals("GeneralFilter")) {
                fileProcessor = new GeneralFilter(eventType);
            } else if (className.equals("MiRNARecognizer")) {
                fileProcessor = new MiRNARecognizer();
            } else if (className.equals("ReplaceEntity")) {
                fileProcessor = new ReplaceEntity();
            } else if (className.equals("RecoverEntity")) {
                fileProcessor = new RecoverEntity();
            } else if (className.equals("ResoluteAnaphoraTarget")) {
                fileProcessor = new ResoluteAnaphora("MiRNA","Gene");
            } else if (className.equals("ResoluteAnaphoraMiRNA")) {
                fileProcessor = new ResoluteAnaphora("Gene","MiRNA");
            } else if (className.equals("LinkCausal")) {
                fileProcessor = new LinkCausal(true);
            } else if (className.equals("ExtractTarget")) {
                fileProcessor = new ExtractTarget();
            } else if (className.equals("ExtractMiRNA")) {
                fileProcessor = new ExtractMiRNA();
            } else if (className.equals("ModifierExtractor")) {
                fileProcessor = new ModifierExtractor(); 
            } else {
                System.err.println("no FileProcessor: " + className);
                System.exit(1);
            }
            BatchProcessor batchProcessor = new BatchProcessor(fileProcessor);
            batchProcessor.processDir(Env.DIR);
        }
    }
}
