package mqtt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MqttMessage {

  private List<String> clientIdList;

  private byte[] message;

  private boolean retain;

  public MqttMessage(byte[] request, String retainFlag){
    this.message = request;
    this.clientIdList = new ArrayList<>();
    this.retain = "1".equalsIgnoreCase(retainFlag);
  }

  public List<String> getClientIdList() {
    return clientIdList;
  }

  public void setClientIdList(List<String> clientIdList) {
    this.clientIdList = clientIdList;
  }

  public byte[] getMessage() {
    return message;
  }

  public void setMessage(byte[] message) {
    this.message = message;
  }

  public boolean isRetain() {
    return retain;
  }

  public byte[] getMessageCopy(){
    return Arrays.copyOf(this.message, this.message.length);
  }
}
