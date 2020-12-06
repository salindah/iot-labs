package mqtt;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class RequestHandler implements Runnable {

  private Socket clientSocket;

  public RequestHandler(Socket socket){
    this.clientSocket = socket;
  }

  public void run(){
    try {
      System.out.println("Request received ...");
      handleRequest(this.clientSocket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void handleRequest(Socket clientSocket) throws IOException {

    byte[] buffer = new byte[1000];
    int size = clientSocket.getInputStream().read(buffer);

    byte[] request = Arrays.copyOfRange(buffer, 0, size);
    byte byte1 = request[0];
    String byte1Bits = byteToBinary(byte1);

    System.out.println("First byte : " + byte1Bits);
    if(byte1Bits.startsWith("0")){
      sendConnectionAck(clientSocket);
    }
    if(byte1Bits.startsWith("1")){
      sendPingResponse(clientSocket);
    }

    System.out.println(size);
  }

  public void sendConnectionAck(Socket clientSocket) throws IOException {
    byte[] response = new byte[4];
    response[0] = convertToByte("00100000").byteValue();
    response[1] = convertToByte("00000010").byteValue();
    response[2] = convertToByte("00000000").byteValue();
    response[3] = convertToByte("00000000").byteValue();

    sendResponse(clientSocket, response);
    System.out.println("Connection ACK sent..");
  }


  public void sendPingResponse(Socket clientSocket) throws IOException {
    byte[] response = new byte[2];
    response[0] = convertToByte("11010000").byteValue();
    response[1] = convertToByte("00000000").byteValue();

    sendResponse(clientSocket, response);
    System.out.println("Ping Response sent..");
  }

  public void sendResponse(Socket clientSocket, byte[] response) throws IOException {
    OutputStream socketOutputStream = clientSocket.getOutputStream();
    socketOutputStream.write(response);
  }


  private Integer convertToByte(String bitSequence){
    if(bitSequence != null && bitSequence.length() == 8){
      char[] seq = bitSequence.toCharArray();

      int bit0 = getIntValue(seq[0]);
      int bit1 = getIntValue(seq[1]);
      int bit2 = getIntValue(seq[2]);
      int bit3 = getIntValue(seq[3]);
      int bit4 = getIntValue(seq[4]);
      int bit5 = getIntValue(seq[5]);
      int bit6 = getIntValue(seq[6]);
      int bit7 = getIntValue(seq[7]);

      int sum = (64 * bit1) + (32 * bit2) + (16 * bit3) + (8 * bit4) + (4 * bit5) + (2 * bit6) + (1 * bit7);
      if(bit0 == 0){
        return new Integer(sum);
      } else {
        return new Integer (sum - 128);
      }
    }
    return null;
  }

  public int getIntValue(char bitValue) {
    if (bitValue == '0') {
      return 0;
    } else if (bitValue == '1') {
      return 1;
    }
    return -1;
  }


  public void printByteArray(byte[] array){
    for(byte b : array){
      //String hexaDecimal = String.format("%02X", b);
      //String hexaDecimal = "".format("0x%x", b);
      //System.out.print( hexaDecimal + " ");

      byteToBinary(b);
    }
    System.out.println("");
  }

  public String byteToBinary(byte b1){
    String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
    return s1;
  }


}
