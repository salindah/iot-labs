package mqtt;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class RequestHandler implements Runnable {

  private Socket clientSocket;

  private String clientId;

  private boolean connected = true;

  public RequestHandler(Socket socket) {
    this.clientSocket = socket;
  }

  public void run() {
    try {
      //Continuously waiting for the new messages from the connection.
      while (this.connected) {
        handleRequest(this.clientSocket);
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void handleRequest(Socket clientSocket) throws IOException, InterruptedException {

    byte[] buffer = new byte[500];
    int requestSize = clientSocket.getInputStream().read(buffer);

    if (requestSize > 0) {
      System.out.println("Request length: " + requestSize);
      byte[] request = Arrays.copyOfRange(buffer, 0, requestSize);
      byte byte1 = request[0];

      String byte1Binary = Util.byteToBinary(byte1);
      String byte1_MSB = byte1Binary.substring(0, 4); //The message type comes in this part.

      String qosLevel = byte1Binary.substring(5, 7); //QOS levels were not implemented other than 0.
      System.out.println("QOS : " + qosLevel);

      // Handle each message by examining the first half of the first byte, which contains the
      // message type.
      System.out.println("First byte : " + byte1_MSB);

      switch (byte1_MSB) {
        case "0001":
          handleConnectionMessage(clientSocket, request);
          break;
        case "1100":
          sendPingResponse(clientSocket);
          break;
        case "1000":
          handleSubscribeMessage(clientSocket, request);
          break;
        case "1010":
          handleUnSubscribeMessage(clientSocket, request);
          break;
        case "0011":
          handlePublishMessage(clientSocket, request, byte1Binary);
          break;
        case "1110":
          disconnectSocket(clientSocket);
          break;
      }
    }
  }


  private void handleConnectionMessage(Socket clientSocket, byte[] request) throws IOException {

    System.out.println("Connection Request received..");

    int protocolLength = Util.getLength(request[2], request[3]);
    int lengthStart = 8 + protocolLength;
    int clientIdStart = 10 + protocolLength;
    int clientIdLength = Util.getLength(request[lengthStart], request[lengthStart + 1]);
    byte[] clientIdByteArray = Arrays
        .copyOfRange(request, clientIdStart, clientIdStart + clientIdLength);

    this.clientId = getTopicName(clientIdByteArray);
    System.out.println("Client ID : " + this.clientId);

    byte[] response = new byte[4];
    response[0] = convertToByte("00100000").byteValue();
    response[1] = convertToByte("00000010").byteValue();
    response[2] = convertToByte("00000000").byteValue();
    response[3] = convertToByte("00000000").byteValue();

    sendResponse(clientSocket, response);
    System.out.println("Connection ACK sent..");
  }


  private void sendPingResponse(Socket clientSocket) throws IOException {

    System.out.println("Ping request received..");

    byte[] response = new byte[2];
    response[0] = convertToByte("11010000").byteValue();
    response[1] = convertToByte("00000000").byteValue();

    sendResponse(clientSocket, response);
    System.out.println("Ping Response sent..");
  }

  private void handleSubscribeMessage(Socket clientSocket, byte[] request) throws IOException {

    //Extract information and add Subscription to the DB.
    System.out.println("Subscribe request received..");
    Subscription subscription = new Subscription(request[2], request[3], clientSocket,
        this.clientId);

    int remainingLength = request[1];
    System.out.println("Remaining Length : " + remainingLength);

    int topicLength = Util.getLength(request[4], request[5]);
    byte[] topicByteArray = Arrays.copyOfRange(request, 6, 6 + topicLength);
    String topicName = getTopicName(topicByteArray);

    System.out.println("Topic Name : " + topicName);
    MqttDB.addSubscription(topicName, subscription);

    //Construct the ACK message and send to the client.
    byte[] response = new byte[5];
    response[0] = convertToByte("10010000").byteValue();
    response[1] = convertToByte("00000011").byteValue(); // remaining length is 3.

    response[2] = request[2];
    response[3] = request[3];

    response[4] = convertToByte("00000000").byteValue(); // set QoS as 0
    sendResponse(clientSocket, response);

    // Send if there are any retained messages for this topic.
    List<MqttMessage> messageList = MqttDB.getMessageList(topicName);
    if (messageList != null && !messageList.isEmpty()) {
      for (MqttMessage message : messageList) {
        if (!message.getClientIdList().contains(this.clientId)) {
          sendResponse(subscription.getClientSocket(), message.getMessageCopy());
          message.getClientIdList().add(this.clientId);
        }
      }
    }

  }


  private void handleUnSubscribeMessage(Socket clientSocket, byte[] request) throws IOException {

    System.out.println("Unsubscribe request received..");
    int topicLength = Util.getLength(request[4], request[5]);
    byte[] topicByteArray = Arrays.copyOfRange(request, 6, 6 + topicLength);
    String topicName = getTopicName(topicByteArray);

    MqttDB.removeSubscription(topicName, clientId);

    //Construct the ACK message and send to the client.
    byte[] response = new byte[4];
    response[0] = convertToByte("10110000").byteValue();
    response[1] = convertToByte("00000010").byteValue(); // remaining length is 2.

    response[2] = request[2];
    response[3] = request[3];
    sendResponse(clientSocket, response);
  }

  private void handlePublishMessage(Socket clientSocket, byte[] request, String byte1Binary)
      throws IOException {

    System.out.println("Publish request received..");
    String retainFlag = byte1Binary.substring(7, 8);

    int remainingLength = request[1];
    System.out.println("Remaining Length : " + remainingLength);

    int topicLength = Util.getLength(request[2], request[3]);
    byte[] topicByteArray = Arrays.copyOfRange(request, 4, 4 + topicLength);
    String topicName = getTopicName(topicByteArray);
    MqttMessage mqttMessage = new MqttMessage(request, retainFlag);

    //Retain the message if it is marked as retain by the client.
    if (mqttMessage.isRetain()) {
      MqttDB.storeMessage(topicName, mqttMessage);
    }

    List<Subscription> subscriberList = MqttDB.getSubscriberList(topicName);
    sendMessageToSubscribers(subscriberList, mqttMessage);
  }

  private void sendMessageToSubscribers(List<Subscription> subscriberList, MqttMessage mqttMessage)
      throws IOException {

    if (subscriberList != null && !subscriberList.isEmpty()) {
      for (Subscription subscription : subscriberList) {
        sendResponse(subscription.getClientSocket(), mqttMessage.getMessageCopy());
        if (mqttMessage.isRetain()) {
          if (!mqttMessage.getClientIdList().contains(subscription.getClientId())) {
            mqttMessage.getClientIdList().add(subscription.getClientId());
          }
        }
      }
    }
  }

  private void disconnectSocket(Socket clientSocket) throws IOException {
    this.connected = false;
    MqttDB.removeClient(clientId);
    clientSocket.close();
  }

  /*
   * A common method to send any given response to the given client.
   * */
  private void sendResponse(Socket clientSocket, byte[] response) throws IOException {
    OutputStream socketOutputStream = clientSocket.getOutputStream();
    socketOutputStream.write(response);
  }

  private Integer convertToByte(String bitSequence) {
    if (bitSequence != null && bitSequence.length() == 8) {
      char[] seq = bitSequence.toCharArray();

      int bit0 = getIntValue(seq[0]);
      int bit1 = getIntValue(seq[1]);
      int bit2 = getIntValue(seq[2]);
      int bit3 = getIntValue(seq[3]);
      int bit4 = getIntValue(seq[4]);
      int bit5 = getIntValue(seq[5]);
      int bit6 = getIntValue(seq[6]);
      int bit7 = getIntValue(seq[7]);

      int sum = (64 * bit1) + (32 * bit2) + (16 * bit3) + (8 * bit4) + (4 * bit5) + (2 * bit6) + (1
          * bit7);
      if (bit0 == 0) {
        return new Integer(sum);
      } else {
        return new Integer(sum - 128);
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

  private String getTopicName(byte[] topicByteArray) {
    String topicName = new String(topicByteArray, 0, topicByteArray.length);
    return topicName;
  }
}
