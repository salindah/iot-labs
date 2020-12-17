package mqtt;

import java.net.Socket;
import java.util.Objects;

public class Subscription {

  private byte[] messageId = new byte[2];

  private String clientId;

  private Socket clientSocket;

  public Subscription(byte msb, byte lsb, Socket clientSocket, String clientId){

    this.messageId[0] = msb;
    this.messageId[1] = lsb;
    this.clientSocket = clientSocket;
    this.clientId = clientId;
  }

  public byte[] getMessageId() {
    return messageId;
  }

  public void setMessageId(byte[] messageId) {
    this.messageId = messageId;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

  public void setClientSocket(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subscription that = (Subscription) o;
    return clientId.equals(that.clientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId);
  }
}
