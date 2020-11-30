package main;

import coapp.CoapClient;

public class MainApplication {

  public static void main(String[] args) {

    CoapClient coapClient = new CoapClient();
    coapClient.run();

    //coapClient.printTest();
  }
}
