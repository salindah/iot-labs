package main;

import coapp.CoapClient;

public class MainApplication {

  public static void main(String[] args) {

    CoapClient coapClient = new CoapClient();

    byte[] message = new byte[3];
    //coapClient.addOptions(message, "sink");

    coapClient.run();

    //byte[] m_GET = coapClient.message_GET("sink");
    //coapClient.sendPacket(m_GET);
  }
}