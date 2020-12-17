package mqtt;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MqttServer {

  public static final int PORT_NUMBER = 1883;

  private ServerSocket serverSocket;

  public void start() {
    try {
      serverSocket = new ServerSocket(PORT_NUMBER);
      System.out.println("MQTT server started ...");

      while (true) {
        Socket clientSocket = null;
        try {

          //Once a new connections is made, handover the task to a separate thread and wait for the
          //next connection.
          clientSocket = serverSocket.accept();
          new Thread(new RequestHandler(clientSocket)).start();

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      stop();
    }
  }

  public void stop() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
