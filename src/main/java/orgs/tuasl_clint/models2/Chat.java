package orgs.tuasl_clint.models2;

import orgs.tuasl_clint.utils.DatabaseConnectionSQLite;
import java.sql.*;


public class Chat {
    private long id;
    private ChatType chatType;
    private String chatName;
    private String chatDescription;
    private String chatPictureUrl;
    private long creatorId;
    private String publicLink;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Chat(Chat chat) {
        this.id = chat.id;
        this.chatType = chat.chatType;
        this.chatName = chat.chatName;
        this.chatDescription = chat.chatDescription;
        this.chatPictureUrl = chat.chatPictureUrl;
        this.creatorId = chat.creatorId;
        this.publicLink = chat.publicLink;
        this.createdAt = chat.createdAt;
        this.updatedAt = chat.updatedAt;
    }


    public enum ChatType {
        PRIVATE, GROUP, CHANNEL, UNKNOWN;

        public static ChatType fromString(String value) {
            for (ChatType type : ChatType.values()) {
                if (type.name().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            //throw new IllegalArgumentException("Invalid ChatType: " + value);
            return UNKNOWN;
        }
    }

    public Chat() {}

    public Chat(ChatType chatType, Timestamp createdAt) {
        this.chatType = chatType;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public Chat(long id, ChatType chatType, String chatName, String chatDescription, String chatPictureUrl, long creatorId, String publicLink, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.chatType = chatType;
        this.chatName = chatName;
        this.chatDescription = chatDescription;
        this.chatPictureUrl = chatPictureUrl;
        this.creatorId = creatorId;
        this.publicLink = publicLink;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public ChatType getChatType() { return chatType; }
    public void setChatType(ChatType chatType) { this.chatType = chatType; }
    public String getChatName() { return chatName; }
    public void setChatName(String chatName) { this.chatName = chatName; }
    public String getChatDescription() { return chatDescription; }
    public void setChatDescription(String chatDescription) { this.chatDescription = chatDescription; }
    public String getChatPictureUrl() { return chatPictureUrl; }
    public void setChatPictureUrl(String chatPictureUrl) { this.chatPictureUrl = chatPictureUrl; }
    public long getCreatorId() { return creatorId; }
    public void setCreatorId(long creatorId) { this.creatorId = creatorId; }
    public String getPublicLink() { return publicLink; }
    public void setPublicLink(String publicLink) { this.publicLink = publicLink; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean save() throws SQLException {
        String sql = "INSERT INTO chats (chat_type, chat_name, chat_description, chat_picture_url, creator_user_id, public_link, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, chatType.name().toLowerCase());
            statement.setString(2, chatName);
            statement.setString(3, chatDescription);
            statement.setString(4, chatPictureUrl);
            statement.setLong(5, creatorId);
            statement.setString(6, publicLink);
            statement.setTimestamp(7, createdAt);
            statement.setTimestamp(8, updatedAt);

            boolean isInserted = statement.executeUpdate() > 0;
            if (isInserted) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = (generatedKeys.getLong(1));
                    }
                }
            }
            return isInserted;
        }
    }

    public boolean update() throws SQLException {
        String sql = "UPDATE chats SET chat_type = ?, chat_name = ?, chat_description = ?, chat_picture_url = ?, creator_user_id = ?, public_link = ?, updated_at = ? WHERE chat_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setString(1, chatType.name().toLowerCase());
            statement.setString(2, chatName);
            statement.setString(3, chatDescription);
            statement.setString(4, chatPictureUrl);
            statement.setLong(5, creatorId);
            statement.setString(6, publicLink);
            statement.setTimestamp(7, updatedAt);
            statement.setLong(8, id);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete() throws SQLException {
        String sql = "DELETE FROM chats WHERE chat_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }
    public void saveOrUpdate() {
        try {
            if(id == 0){
                save();
            }else {
                update();
            }
        } catch (SQLException e) {
            System.out.println("----- Cannot Save Or Update The Chat : "+this.toString());
        }
    }
    @Override
    public String toString() {
        return "Chat{" +
                "chatId=" + id +
                ", chatType=" + chatType +
                ", chatName='" + chatName + '\'' +
                ", chatDescription='" + (chatDescription != null ? chatDescription : "null") + '\'' +
                ", chatPictureUrl='" + (chatPictureUrl != null ? chatPictureUrl : "null") + '\'' +
                ", creatorUserId=" + creatorId +
                ", publicLink='" + (publicLink != null ? publicLink : "null") + '\'' +
                ", createdAt=" + (createdAt != null ? createdAt.toString() : "null") +
                ", updatedAt=" + (updatedAt != null ? updatedAt.toString() : "null") +
                '}';
    }
}
