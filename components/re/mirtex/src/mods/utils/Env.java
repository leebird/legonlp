package mods.utils;

public class Env {

    public static String basedir = "data/";

    public static String subdir = "test/";
    

    public static final String tredir = "tregex/";

    public static String TRIGGER = "trigger/trigger.txt";

    //public static String DATA_SET = "BioNLP-ST_2011_genia_train_data_rev1";

    public static String DATA_SET = "text/";

//    public static String DATA_SET = "text/";
    

    public static  String DIR = basedir + DATA_SET;

    public static  String DIR_PARSE = basedir + "parse/";

    public static  String DIR_SIMP = basedir + "simp/";

    public static  String DIR_REF = basedir + "ref/";

    // predicate argument
    public static final String BASIC_PATTERN_TREGEX = tredir+"basic pattern.txt";

    public static final String NULL_PATTERN_TREGEX      = tredir+"null pattern.txt";

    public static final String AGENT_PATTERN_TREGEX = tredir+"agent.txt";

    public static final String THEME_PATTERN_TREGEX      = tredir+"theme.txt";

    // simplification
    public static final String COORDINATION_TREGEX      = tredir+"coordination.txt";

    public static final String RELATIVE_TREGEX          = tredir+"relative clause.txt";

    public static final String PARENTHESIS_TREGEX       = tredir+"parenthesis.txt";

    public static final String APPOSITION_TREGEX        = tredir+"apposition.txt";

    public static final String NPPP_TREGEX              = tredir+"nppp.txt";

    public static final String SENBEG_TREGEX            = tredir+"sentenceBeginning.txt";

    public static final String OTHERS_TREGEX            = tredir+"others.txt";

    // ref
    public static final String REFEXTRACTOR_TREGEX      = tredir+"reference.txt";

    public static final String MEMBER_COLLECTION_TREGEX = tredir+"member-collection.txt";

    public static final String PART_WHOLE_TREGEX        = tredir+"part-whole.txt";
    
    public static final String CAUSE_TREGEX				= tredir+"cause.txt";
    
    public static final String EFFECT_TREGEX			= tredir+"effect.txt";
    
    public static final String ENTITY_REPLACE			= "N[0-9]+ENTITY";

	public static final String MODIFIER_TREGEX = tredir+"modifier.txt";
	
	public static void setBaseDir(String dir) {
		basedir = dir;
		DIR = basedir + DATA_SET;

		DIR_PARSE = basedir + "parse/";

		DIR_SIMP = basedir + "simp/";

		DIR_REF = basedir + "ref/";
        }
}
