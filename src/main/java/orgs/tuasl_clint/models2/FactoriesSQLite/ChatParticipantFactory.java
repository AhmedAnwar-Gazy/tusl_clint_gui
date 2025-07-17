package orgs.tuasl_clint.models2.FactoriesSQLite;

import orgs.tuasl_clint.models2.ChatParticipant;
import orgs.tuasl_clint.utils.DatabaseConnectionSQLite;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class ChatParticipantFactory {
    public static ChatParticipant create() {
        return new ChatParticipant();
    }

    public static ChatParticipant createFromResultSet(ResultSet rs) throws SQLException {
        return new ChatParticipant(
                (rs.getLong("chat_participant_id")),
                (rs.getLong("chat_id")),
                (rs.getLong("user_id")),
                ChatParticipant.ChatParticipantRole.fromString(rs.getString("role")),
                rs.getTimestamp("joined_at"),
                rs.getTimestamp("muted_until"),
                rs.getInt("is_pinned") == 1,
                rs.getInt("unread_count"),
                rs.getLong("last_read_message_id") != 0 ? (rs.getLong("last_read_message_id")) : 0
        );
    }

    public static ChatParticipant findById(long chatParticipantId) throws SQLException {
        String sql = "SELECT * FROM chat_participants WHERE chat_participant_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, chatParticipantId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return createFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static List<ChatParticipant> findByChatId(long chatId) throws SQLException {
        String sql = "SELECT * FROM chat_participants WHERE chat_id = ?";
        List<ChatParticipant> participants = new ArrayList<>();
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, chatId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    participants.add(createFromResultSet(rs));
                }
            }
        }
        return participants;
    }
    public static int getChatParticipantCount(long chatId)throws SQLException{
        String query = "SELECT count(*) as Count FROM chat_participants WHERE chat_id = ?";
        try(PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(query)){
            statement.setLong(1,chatId);
            ResultSet rs = statement.executeQuery();
            if(rs.next())
                return rs.getInt("Count");
        }
        return -1;
    }

    public static List<ChatParticipant> findByUserId(long userId) throws SQLException {
        String sql = "SELECT * FROM chat_participants WHERE user_id = ?";
        List<ChatParticipant> participants = new ArrayList<>();
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    participants.add(createFromResultSet(rs));
                }
            }
        }
        return participants;
    }

    public static ChatParticipant findByChatAndUser(long chatId, long userId) throws SQLException {
        String sql = "SELECT * FROM chat_participants WHERE chat_id = ? AND user_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, chatId);
            statement.setLong(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return createFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static List<ChatParticipant> findByRole(ChatParticipant.ChatParticipantRole role) throws SQLException {
        String sql = "SELECT * FROM chat_participants WHERE role = ?";
        List<ChatParticipant> participants = new ArrayList<>();
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setString(1, role.name().toLowerCase());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    participants.add(createFromResultSet(rs));
                }
            }
        }
        return participants;
    }

    public static List<ChatParticipant> getAll() {
        List<ChatParticipant> participants = new ArrayList<>();
        String sql = "SELECT * FROM chat_participants";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    participants.add(createFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("----- Cannot Get All Participants From DataBase");
        }
        return participants;
    }
}