package rest.ws;

import java.util.Arrays;

import gqltosql.schema.DChannel;
import gqltosql.schema.DMessage;
import gqltosql.schema.DModel;

public class TemplateChannel {
    private String hash;
    
    private DChannel channel;
    private DMessage[] messages;
    private int[] mapping;

    public TemplateChannel(DChannel ch, int size) {
        this.setChannel(ch);
        this.messages = new DMessage[size];
        if (ch != null) {
            this.mapping = new int[ch.getMessages().length];
            Arrays.fill(mapping, -1);
        }
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
		return hash;
	}
    
    public DChannel getChannel() {
        return channel;
    }

    public void setChannel(DChannel ch) {
        this.channel = ch;
    }

    public DMessage[] getMessages() {
        return messages;
    }

    public void setMessages(DMessage[] messages) {
        this.messages = messages;
    }
    
    public void addMessage(int idx, DMessage f) {
        this.messages[idx] = f;
        if (f != null) {
            this.mapping[f.getIndex()] = idx;
        }
    }
    
    public int getClientMessageIndex(int serverIdx) {
        return mapping[serverIdx];
    }
}
