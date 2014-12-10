package mods.extractor.pattern;

public class BioNLPPatternFactory implements PatternFactory {

  private static BioNLPPatternFactory pf = null;

  private BioNLPPatternFactory() {
  }

    public static BioNLPPatternFactory instance() {
        if (pf == null) {
            pf = new BioNLPPatternFactory();
        }
        return pf;
  }

  @Override
  public ExtractorPattern compile(String tregex, String name) {
    ExtractorPattern pattern = null;
    if (name.equals("Arg Vact")) {
      pattern = new ArgVact(tregex);
    } else if (name.equals("Arg Vpass")) {
      pattern = new ArgVpass(tregex);
    } else if (name.equals("Arg Vadj")) {
      pattern = new ArgVadj(tregex);
    } else if (name.equals("Arg - Vnorm")) {
      pattern = new ArgDashVnorm(tregex);
    } else if (name.equals("Arg - expressing")) {
      pattern = new ArgDashExpressing(tregex);
    } else if (name.equals("Arg Vnorm")) {
      pattern = new ArgVnorm(tregex);
    } else if (name.equals("Vadj Arg")) {
      pattern = new VadjArg(tregex);
    } else if (name.equals("Arg does something by Vvbg")) {
      pattern = new ByVbg(tregex);
    } else if (name.equals("Arg does something through -ion")) {
      pattern = new ThroughVion(tregex);
    } else if (name.equals("NN locus")
        || name.equals("NN JJ locus")
        || name.equals("NN promoter region")) {
      pattern = new ArgLocus(tregex);
    } else if (name.equals("Target")) {
        pattern = new Target(tregex);
    } else {
      pattern = new ExtractorPattern(tregex);
    }
    pattern.setName(name);
    return pattern;
  }
}
