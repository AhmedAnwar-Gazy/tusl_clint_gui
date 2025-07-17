package orgs.tuasl_clint.models2;

import orgs.tuasl_clint.models2.FactoriesSQLite.UserFactory;
import orgs.tuasl_clint.utils.DatabaseConnectionSQLite;
import orgs.tuasl_clint.utils.TimeStampHelperClass;

import java.sql.*;
import java.util.Date;


public class User {
    private long id;
    private String phoneNumber;
    private String username;
    private String firstName;
    private String lastName;
    private String bio;
    private String profilePictureUrl;
    private String password;
    private String twoFactorSecret;
    private Timestamp lastSeenAt;
    private boolean isOnline;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    public static User user;

    public User() {}

    public User(String phoneNumber, String firstName, Timestamp createdAt) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.createdAt = createdAt;
    }

    public User(long userId, String phoneNumber, String username, String firstName, String lastName, String bio, String profilePictureUrl, String hashedPassword, String twoFactorSecret, Timestamp lastSeenAt, boolean isOnline, Timestamp createdAt, Timestamp updatedAt) {
        this.id = userId;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.password = hashedPassword;
        this.twoFactorSecret = twoFactorSecret;
        this.lastSeenAt = lastSeenAt;
        this.isOnline = isOnline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public User(String username, String phone, String password) {
        this.username = username;
        this.phoneNumber = phone;
        this.password = password;
        this.createdAt = new Timestamp(new Date().getTime());
        this.lastSeenAt = createdAt;
        this.updatedAt = createdAt;
        this.firstName = username;
        this.lastName = username;
        this.bio = username;
    }

    public long getId() {return id; }
    public void setId(long id) { this.id = id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }
    public Timestamp getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Timestamp lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean save() throws SQLException {
        String sql = "INSERT INTO users (phone_number, username, first_name, last_name, bio, profile_picture_url, hashed_password, two_factor_secret, last_seen_at, is_online, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, phoneNumber);
            statement.setString(2, username);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, bio);
            statement.setString(6, profilePictureUrl);
            statement.setString(7, password);
            statement.setString(8, twoFactorSecret);
            statement.setTimestamp(9, lastSeenAt);
            statement.setInt(10, isOnline ? 1 : 0);
            statement.setTimestamp(11, createdAt);
            statement.setTimestamp(12, updatedAt);

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
        String sql = "UPDATE users SET phone_number = ?, username = ?, first_name = ?, last_name = ?, bio = ?, profile_picture_url = ?, hashed_password = ?, two_factor_secret = ?, last_seen_at = ?, is_online = ?, updated_at = ? WHERE user_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setString(1, phoneNumber);
            statement.setString(2, username);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, bio);
            statement.setString(6, profilePictureUrl);
            statement.setString(7, password);
            statement.setString(8, twoFactorSecret);
            statement.setTimestamp(9, lastSeenAt);
            statement.setInt(10, isOnline ? 1 : 0);
            statement.setTimestamp(11, updatedAt);
            statement.setLong(12, id);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete() throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }
    public boolean saveOrUpdate(){
        try {
            if(id == 0){
                return save();
            }else {
                User u = UserFactory.findById(this.id);
                u = this;
                return update();
            }
        } catch (SQLException e) {
            System.err.println("\n\n---------------Save Or Update------------- User: Error "+e.getMessage()+" : "+this.toString()+"");
            e.printStackTrace();
            System.out.println("\n\n");
            return false;
        }
    }
    @Override
    public String toString() {
        return "User{" +
                "userId=" + id +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", bio='" + bio + '\'' +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", password=" + this.password +// Masked for security
                ", twoFactorSecret=" + this.twoFactorSecret +  // Masked for security
                ", lastSeenAt=" + lastSeenAt +
                ", isOnline=" + isOnline +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
        '}';
    }
}