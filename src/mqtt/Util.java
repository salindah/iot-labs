package mqtt;

public class Util {

  public static String byteToBinary(byte b1){
    return String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
  }

  public static String byteArrayToString(byte[] byteArray){
    if(byteArray != null && byteArray.length > 0){
      return new String(byteArray, 0, byteArray.length);
    }
    return "";
  }

  public static int getLength(byte msb, byte lsb) {
    String length = byteToBinary(msb) + byteToBinary(lsb);
    return Integer.parseInt(length, 2);
  }

  public static void printByteArray(byte[] array){
    int count = 0;
    for(byte b : array){

      //String hexaDecimal = String.format("%02X", b);
      String hexaDecimal = "".format("0x%x", b);
      System.out.println( count + " : " + hexaDecimal);
      count++;
      //System.out.println(byteToBinary(b));
    }
    System.out.println("");
  }
}
