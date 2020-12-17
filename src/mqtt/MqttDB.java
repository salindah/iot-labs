package mqtt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttDB {

  private static Map<String, List<Subscription>> topicToSubscriptionListMap = Collections
      .synchronizedMap(new HashMap<>());

  private static Map<String, List<MqttMessage>> topicToMessageMap = Collections
      .synchronizedMap(new HashMap<>());


  public static void addSubscription(String topic, Subscription subscription) {
    if (topic != null && subscription != null) {
      List<Subscription> clientList = topicToSubscriptionListMap.get(topic);
      if (clientList == null) {
        clientList = new ArrayList<>();
      }
      if(!clientList.contains(subscription)){
        clientList.add(subscription);
      }
      topicToSubscriptionListMap.put(topic, clientList);
    }
  }

  public static List<Subscription> getSubscriberList(String topic){
    if(topic != null){
      return topicToSubscriptionListMap.get(topic);
    }
    return null;
  }

  public static void removeSubscription(String topic, String clientId){
    if(topic != null && clientId != null){
      List<Subscription> clientList = topicToSubscriptionListMap.get(topic);
      if(!clientList.isEmpty()){
        clientList.removeIf(c -> c.getClientId().equals(clientId));
      }
    }
  }

  public static void removeClient(String clientId){
    //Iterate over existing topics and remove from everyone if this client exists.
    for(List<Subscription> topic : topicToSubscriptionListMap.values()){
      topic.removeIf(subscription -> clientId.equalsIgnoreCase(subscription.getClientId()));
    }
  }

  public static void storeMessage(String topicName, MqttMessage mqttMessage){

    if( topicName != null && mqttMessage != null ){
      List<MqttMessage> messageList = topicToMessageMap.get(topicName);
      if(messageList == null){
        messageList = new ArrayList<>();
        messageList.add(mqttMessage);
        topicToMessageMap.put(topicName, messageList);
      } else {
        messageList.add(mqttMessage);
      }
    }
  }

  public static List<MqttMessage> getMessageList(String topic){
    if(topic != null){
      return topicToMessageMap.get(topic);
    }
    return null;
  }

}
