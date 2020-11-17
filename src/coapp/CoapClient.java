package coapp;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class CoapClient {

  public static final String INVALID_INPUT_MSG = "Invalid input was entered. Please enter one option put of above.";

  public static final String SERVER_NAME = "coap.me";

  public static final int PORT_NUMBER = 5683;

  private DatagramSocket socket;

  private InetAddress address;

  private byte[] sendingBuffer;

  private byte[] receivingBuffer;

  private boolean running = true;

  public CoapClient() {
    try {
      socket = new DatagramSocket();
      address = InetAddress.getByName(SERVER_NAME);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() {
    System.out.println("+++++++++++++++++++ Welcome to the COAP Client +++++++++++++++++++");
    System.out.println("Enter 1 for a GET request");
    System.out.println("Enter 2 for a POST request");
    System.out.println("Enter 3 for a PUT request");
    System.out.println("Enter 4 for a DELETE request");
    System.out.println("Enter 5 to exit");

    while (running){
      try {
        Scanner scanner = new Scanner(System.in);
        int requestType = scanner.nextInt();
        if (isInputValid(requestType)) {
          handleRequestType(requestType);
        } else {
          System.out.println(INVALID_INPUT_MSG);
        }

      } catch (Exception e) {
        System.out.println(INVALID_INPUT_MSG);
      }
    }
  }

  private void handleRequestType(int requestType){

    if(requestType == 1){
      handleGetRequest();
    } else if (requestType == 2){
      handlePostRequest();
    } else if (requestType == 3) {
      handlePutRequest();
    } else if (requestType == 4){
      handleDeleteRequest();
    } else {
      exit();
    }
  }

  private void handleGetRequest(){
    System.out.println("Please enter the URI path in the below line:");
    Scanner scanner = new Scanner(System.in);
    String uriPath = scanner.nextLine();
    byte[] message = constructMessage(uriPath);

    sendMessage(message);
  }

  private void handlePostRequest(){
    System.out.println("POST");
  }

  private void handlePutRequest(){
    System.out.println("PUT");
  }

  private void handleDeleteRequest(){
    System.out.println("DELETE");
  }

  private void exit(){
    this.running = false;
    socket.close();
    System.out.println("++++++++++ Good Bye +++++++++++");
  }

  private boolean isInputValid(int input) {
    if (input == 1 || input == 2 || input == 3 || input == 4 || input == 5) {
      return true;
    }
    return false;
  }

  private byte[] constructMessage(String uriPath){
    byte[] message = new byte[100];
    addHeader(message, uriPath);


    return message;
  }

  private void addHeader(byte[] message, String uriPath){

    message[0] = convertToByte("01010000").byteValue();
    message[1] = convertToByte("00000001").byteValue();
    message[2] = convertToByte("10101010").byteValue();
    message[3] = convertToByte("01010101").byteValue();

    addOptions(message, uriPath);
  }

  public void addOptions(byte[] message, String uriPath ){
    if(uriPath != null){

      Integer uriOptionType = new Integer(11);
      String uriOptionTypeStr = addLeadingZeros(Integer.toBinaryString(uriOptionType));


      Integer pathLength = uriPath.length();
      String uriPathStr = addLeadingZeros(Integer.toBinaryString(pathLength));


      message[4] = convertToByte(uriOptionTypeStr + uriPathStr).byteValue();

      char[] pathArray = uriPath.toCharArray();
      for(int i = 0; i < pathLength; i++ ){
        int index = 5 + i;
        message[index] = (byte) pathArray[i];
      }

      System.out.println(message);
    }
  }

  public void sendMessage(byte[] message) {
    DatagramPacket packet
        = new DatagramPacket(message, message.length, address, PORT_NUMBER);
    try {

      System.out.println(message.length);
      socket.send(packet);
      System.out.println("Packet send to coap server ...");

      byte[] receivingBuffer = new byte[65535];
      packet = new DatagramPacket(receivingBuffer, receivingBuffer.length);
      socket.receive(packet);
      System.out.println("Response received from the server ...");
      String received = new String(
          packet.getData(), 0, packet.getLength());

      System.out.println(received);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public byte[] message_GET(String path) {

    sendingBuffer = new byte[10];
    byte byte1 = (byte) 0b0101_0000;
    byte byte2 = (byte) 0b0000_0001;
    byte byte3 = (byte) 0b1010_1010;
    byte byte4 = (byte) 0b0101_0101;

    sendingBuffer[0] = byte1;
    sendingBuffer[1] = byte2;
    sendingBuffer[2] = byte3;
    sendingBuffer[3] = byte4;

    if (path != null) {
      Integer pathLength = path.length();
      String format = addLeadingZeros(Integer.toBinaryString(pathLength));
      System.out.println(format);

      byte[] optionValueByteArray = path.getBytes();

//      for(int i = 0; i < optionValueByteArray.length; i++){
//        System.out.println(optionValueByteArray[i]);
//      }
    }

    return sendingBuffer;
  }


  public String addLeadingZeros(String value) {
    return ("0000" + value).substring(value.length());
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


  public void testConvert(){

//    System.out.println(  convertTotoByte("10000000") );
//    System.out.println(  convertTotoByte("10000001") );
//    System.out.println(  convertTotoByte("10000011") );
//    System.out.println(  convertTotoByte("10000111") );
//    System.out.println(  convertTotoByte("10001111") );
//
//    System.out.println(  convertTotoByte("10011111") );
//    System.out.println(  convertTotoByte("10111111") );

      Integer test = convertToByte("01111111");
      System.out.println(test.byteValue());


  }

  public void printTest() {

    byte aByte0 = (byte) 0b10000000;
    byte aByte1 = (byte) 0b10000001;
    byte aByte2 = (byte) 0b10000011;
    byte aByte3 = (byte) 0b10000111;
    byte aByte4 = (byte) 0b10001111;
    byte aByte5 = (byte) 0b10011111;
    byte aByte6 = (byte) 0b10111111;
    byte aByte7 = (byte) 0b11111111;

    byte aByte00 = (byte) 0b00000000;
    byte aByte11 = (byte) 0b00000001;
    byte aByte22 = (byte) 0b00000011;
    byte aByte33 = (byte) 0b00000111;
    byte aByte44 = (byte) 0b00001111;
    byte aByte55 = (byte) 0b00011111;
    byte aByte66 = (byte) 0b00111111;
    byte aByte77 = (byte) 0b01111111;

    System.out.println(aByte0);
    System.out.println(aByte1);
    System.out.println(aByte2);
    System.out.println(aByte3);
    System.out.println(aByte4);
    System.out.println(aByte5);
    System.out.println(aByte6);
    System.out.println(aByte7);

    System.out.println("-----------------------------");

    System.out.println(aByte00);
    System.out.println(aByte11);
    System.out.println(aByte22);
    System.out.println(aByte33);
    System.out.println(aByte44);
    System.out.println(aByte55);
    System.out.println(aByte66);
    System.out.println(aByte77);
  }


}
