import java.util.*;

public class Message {
    private String messageId;
    private String senderId;
    private String content;
    private Date timestamp;
    private String parentMessageId; // For replies
    private boolean isReply;

    public Message(String messageId, String senderId, String content) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = new Date();
        this.parentMessageId = null;
        this.isReply = false;
    }

    // Constructor for reply
    public Message(String messageId, String senderId, String content, String parentMessageId) {
        this(messageId, senderId, content);
        this.parentMessageId = parentMessageId;
        this.isReply = true;
    }

    // Getters
    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getParentMessageId() {
        return parentMessageId;
    }

    public boolean isReply() {
        return isReply;
    }

    @Override
    public String toString() {
        return "[" + new java.text.SimpleDateFormat("HH:mm:ss").format(timestamp) + "] " + 
               senderId + ": " + content + 
               (isReply ? " (Reply to: " + parentMessageId + ")" : "");
    }
}
