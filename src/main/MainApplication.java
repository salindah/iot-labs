package main;

import coapp.CoapClient;
import mqtt.MqttServer;
import mqtt.MultiThreadedServer;

public class MainApplication {

  public static void main(String[] args) {

    //CoapClient coapClient = new CoapClient();
    //coapClient.run();
//
    MqttServer mqttServer = new MqttServer();
    mqttServer.start();

    //MultiThreadedServer server = new MultiThreadedServer(1883);
    //new Thread(server).start();

  }
}
