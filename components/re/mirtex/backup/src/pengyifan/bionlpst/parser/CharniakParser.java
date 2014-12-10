package pengyifan.bionlpst.parser;

import pengyifan.bionlpst.v2.Env;
import pengyifan.bionlpst.v2.FileProcessor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

public class CharniakParser extends FileProcessor{

    private static final  String PARSER = "sh /home/leebird/nlp/parser/charniak/bllip-parser-master/parse.sh ";

    private String rawText;
    
    public static void main(String[] args)
        throws Exception {
        CharniakParser p = new CharniakParser();
        p.parse();
    }

public String parse()
    throws Exception {
    String parserOutput = "";
    String s = "";
    Runtime rt = Runtime.getRuntime();
    Process proc = rt.exec(PARSER+rawText);

    BufferedReader stdInput = new BufferedReader(new 
                                                 InputStreamReader(proc.getInputStream()));

    BufferedReader stdError = new BufferedReader(new 
                                                 InputStreamReader(proc.getErrorStream()));

    System.out.println(PARSER+rawText);
    System.out.println("Here is the standard output of the command:\n");
    while ((s = stdInput.readLine()) != null) {
        parserOutput += s;
        System.out.println(s);
    }

    System.out.println("Here is the standard error of the command (if any):\n");
    while ((s = stdError.readLine()) != null) {
        System.out.println(s);
    }
    return parserOutput;
}

    @Override
    public void readResource(String dir, String filenamee)
        {
            rawText = dir + "/" + filename+".txt";
            String output = "";

            try {
                output = parse();
            } catch (Exception e) {
                System.exit(1);
            }
            try {
                PrintStream out = new PrintStream(new FileOutputStream(Env.DIR_PARSE
                                                                       + "/"
                                                                       + filename
                                                                       + ".ptb"));
                out.println(output);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
}