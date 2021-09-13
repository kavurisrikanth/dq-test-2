package gqltosql.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import d3e.core.MapExt;

public class DChannel {
  private String name;
  private int index;
  private DMessage[] messages;
  private Map<String, DMessage> messagesByName;
  
  public DChannel(String name, int index, int msgCount) {
    this.name = name;
    this.index = index;
    this.messages = new DMessage[msgCount];
    this.messagesByName = new HashMap<>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public DMessage[] getMessages() {
    return messages;
  }
  
  public DMessage getMessage(int idx) {
    return messages[idx];
  }
  
  public DMessage getMessage(String name) {
    return messagesByName.get(name);
  }

  public void setMessages(DMessage[] messages) {
    this.messages = messages;
    this.messagesByName = MapExt.fromIterable(Arrays.asList(messages), x -> x.getName(), (x) -> x);
  }
  
  public void addMessage(int index, DMessage msg) {
    this.messages[index] = msg;
    this.messagesByName.put(msg.getName(), msg);
  }
}
