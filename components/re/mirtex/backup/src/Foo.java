import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Foo {

  /**
   * @param args
   */
  public static void main(String[] args) {
    String regex = "(?!DNA[-]).*[-]";
    Matcher m = Pattern.compile(regex).matcher("DNA-");
    if (m.find()) {
      System.out.println("t");
    } else {
      System.out.println("f");
    }
  }

}
