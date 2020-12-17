package main;

import mqtt.MqttServer;

public class MainApplication {

  public static void main(String[] args) {

    //CoapClient coapClient = new CoapClient();
    //coapClient.run();
//
    MqttServer mqttServer = new MqttServer();
    mqttServer.start();

  }
}
