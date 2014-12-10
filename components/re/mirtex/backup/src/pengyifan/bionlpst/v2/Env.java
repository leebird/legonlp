package pengyifan.bionlpst.v2;

public class Env {

    public static final String basedir = "data/";

    public static String subdir = "bionlp2011/training/";

    public static final String tredir = "tregex/";

    public static String TRIGGER = "BioNLP-ST_2011_genia_train_data_rev1.EventTrigger4";

    public static String DATA_SET = "BioNLP-ST_2011_genia_train_data_rev1";

    public static final String DIR = basedir + subdir + DATA_SET;

    public static final String DIR_PARSE = basedir + subdir + "parse/";

    public static final String DIR_SIMP = basedir + subdir + "simp/";

    public static final String DIR_REF = basedir + subdir + "ref/";

    // predicate argument
    public static final String BASIC_PATTERN_TREGEX = tredir+"basic pattern.txt";

    public static final String NULL_PATTERN_TREGEX      = tredir+"null pattern.txt";

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
}
