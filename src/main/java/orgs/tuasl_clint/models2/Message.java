package orgs.tuasl_clint.models2;

import orgs.tuasl_clint.utils.DatabaseConnectionSQLite;

import java.sql.*;
import java.util.Date;

public class Message {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String messageType;
    private String content;
    private Long mediaId;
    private Long repliedToMessageId;
    private Long forwardedFromUserId;
    private Long forwardedFromChatId;
    private Timestamp sentAt;
    private Timestamp editedAt;
    private Boolean isDeleted;
    private Integer viewCount;
    private Media media;

    public void setMedia(Media media) {
        this.media = media;
        if(media != null){
            this.messageType = media.getMediaType();
            this.mediaId = media.getId();
        }else {
            this.messageType = "text";
            this.mediaId = 0L;
        }
    }

    public Message(Long id) {
        this.id = id;
    }
    public Message(String content) {
        this.content = content;
        this.sentAt = new Timestamp(new Date().getTime());
        this.editedAt = sentAt;
    }

    public Message(Long id, Long chatId, Long senderId, String messageType, String content, Long mediaId, Long repliedToMessageId, Long forwardedFromUserId, Long forwardedFromChatId, Timestamp sentAt, Timestamp editedAt, Boolean isDeleted, Integer viewCount) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.messageType = messageType;
        this.content = content;
        this.mediaId = mediaId;
        this.repliedToMessageId = repliedToMessageId;
        this.forwardedFromUserId = forwardedFromUserId;
        this.forwardedFromChatId = forwardedFromChatId;
        this.sentAt = sentAt;
        this.editedAt = editedAt;
        this.isDeleted = isDeleted;
        this.viewCount = viewCount;
    }

    public Long getId() { return id; }
    public Long getChatId() { return chatId; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMessageType() { return messageType; }
    public String getContent() { return content; }
    public Long getMediaId() { return mediaId; }
    public Long getRepliedToMessageId() { return repliedToMessageId; }
    public Long getForwardedFromUserId() { return forwardedFromUserId; }
    public Long getForwardedFromChatId() { return forwardedFromChatId; }
    public Timestamp getSentAt() { return sentAt; }
    public Timestamp getEditedAt() { return editedAt; }
    public Boolean getDeleted() { return isDeleted; }
    public Integer getViewCount() { return viewCount; }
    public void setId(Long id) { this.id = id; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public void setContent(String content) { this.content = content; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }
    public void setRepliedToMessageId(Long repliedToMessageId) { this.repliedToMessageId = repliedToMessageId; }
    public void setForwardedFromUserId(Long forwardedFromUserId) { this.forwardedFromUserId = forwardedFromUserId; }
    public void setForwardedFromChatId(Long forwardedFromChatId) { this.forwardedFromChatId = forwardedFromChatId; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
    public void setEditedAt(Timestamp editedAt) { this.editedAt = editedAt; }
    public void setDeleted(Boolean deleted) { isDeleted = deleted; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public boolean save() throws SQLException {
        String sql = "INSERT INTO messages (chat_id, sender_user_id, message_type, content, media_id, replied_to_message_id, forwarded_from_user_id, forwarded_from_chat_id, sent_at, edited_at, is_deleted, view_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, chatId);
            statement.setLong(2, senderId != null ? senderId : -1);
            statement.setString(3, messageType);
            statement.setString(4, content);
            statement.setObject(5, mediaId);
            statement.setObject(6, repliedToMessageId);
            statement.setObject(7, forwardedFromUserId);
            statement.setObject(8, forwardedFromChatId);
            statement.setTimestamp(9, sentAt);
            statement.setTimestamp(10, editedAt);
            statement.setBoolean(11, isDeleted != null ? isDeleted : false);
            statement.setInt(12, viewCount != null ? viewCount : 0);

            boolean isInserted = statement.executeUpdate() > 0;
            if (isInserted) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getLong(1);
                    }
                }
            }
            return isInserted;
        }
    }

    public boolean update() throws SQLException {
        String sql = "UPDATE messages SET content = ?, edited_at = ?, is_deleted = ?, view_count = ? WHERE message_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setString(1, content);
            statement.setTimestamp(2, editedAt);
            statement.setBoolean(3, isDeleted != null ? isDeleted : false);
            statement.setInt(4, viewCount != null ? viewCount : 0);
            statement.setLong(5, id);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete() throws SQLException {
        String sql = "DELETE FROM messages WHERE message_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public Media getMedia() {
        return this.media;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + id +
                ", chatId=" + chatId +
                ", senderUserId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", mediaId=" + mediaId +
                ", repliedToMessageId=" + repliedToMessageId +
                ", forwardedFromUserId=" + forwardedFromUserId +
                ", forwardedFromChatId=" + forwardedFromChatId +
                ", sentAt=" + sentAt +
                ", editedAt=" + editedAt +
                ", isDeleted=" + isDeleted +
                ", viewCount=" + viewCount +
                ", media=" + (media != null ? media.toString() : "null") +
                '}';
    }

    public void saveOrUpdate() {
        try {
            save();
        } catch (SQLException e) {
            try {
                update();
            } catch (SQLException ex) {
                System.out.println("----- Cannot Save Or Update Messsage : "+ toString());
                e.printStackTrace();
            }
        }
    }
}