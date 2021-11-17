public class test {

  public static void main(String[] args) {
    String bits = Integer.toBinaryString(64 & 0xFF).replace(' ', '0');

    //.replace(' ', '0');
    StringBuilder ret  = new StringBuilder();
    System.out.println(ret.toString());
  }
}
