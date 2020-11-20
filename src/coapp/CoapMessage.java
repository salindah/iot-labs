package coapp;

import java.util.Arrays;

public class CoapMessage {

  private byte[] buffer;

  private int length;

  public CoapMessage(){
    buffer = new byte[100];
    length = 0;
  }

  public void addToBuffer(byte element){

    if(this.length < 100){
      buffer[this.length] = element;
      this.length++;
    }
  }

  public byte[] getMessage(){
    byte[] message = Arrays.copyOfRange(this.buffer, 0, this.length);
    return message;
  }

  public int getLength() {
    return length;
  }

  public void printByteArray(byte[] array){
    for(byte b : array){
      int val = b;
      System.out.print( val + " ");
    }
    System.out.println("");
  }
}
