package orgs.tuasl_clint.models2;

import orgs.tuasl_clint.utils.DatabaseConnectionSQLite;
import java.sql.*;


public class Media {
    private long id;
    private long uploadedByUserId;
    private String fileName;
    private String filePathOrUrl;
    private String mediaType;
    private long fileSize;
    private String thumbnailUrl;
    private Integer durationSeconds;
    private Integer width;
    private Integer height;
    private Timestamp uploadedAt;
    private String transferId;


    public Media(long id, long uploadedByUserId, String fileName, String filePathOrUrl, String mediaType, long fileSize, String thumbnailUrl, Integer durationSeconds, Integer width, Integer height, Timestamp uploadedAt, String transferId) {
        this.id = id;
        this.uploadedByUserId = uploadedByUserId;
        this.fileName = fileName;
        this.filePathOrUrl = filePathOrUrl;
        this.mediaType = mediaType;
        this.fileSize = fileSize;
        this.thumbnailUrl = thumbnailUrl;
        this.durationSeconds = durationSeconds;
        this.width = width;
        this.height = height;
        this.uploadedAt = uploadedAt;
        this.transferId = transferId;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public Media() {}

    public Media(String fileName, String filePathOrUrl, String mediaType, long fileSize, Timestamp uploadedAt) {
        this.fileName = fileName;
        this.filePathOrUrl = filePathOrUrl;
        this.mediaType = mediaType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public Media(long id, long uploadedByUserId, String fileName, String filePathOrUrl, String mediaType, long fileSize, String thumbnailUrl, Integer durationSeconds, Integer width, Integer height, Timestamp uploadedAt) {
        this.id = id;
        this.uploadedByUserId = uploadedByUserId;
        this.fileName = fileName;
        this.filePathOrUrl = filePathOrUrl;
        this.mediaType = mediaType;
        this.fileSize = fileSize;
        this.thumbnailUrl = thumbnailUrl;
        this.durationSeconds = durationSeconds;
        this.width = width;
        this.height = height;
        this.uploadedAt = uploadedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(long uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePathOrUrl() { return filePathOrUrl; }
    public void setFilePathOrUrl(String filePathOrUrl) { this.filePathOrUrl = filePathOrUrl; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }

    public boolean save() throws SQLException {
        String sql = "INSERT INTO media (uploader_user_id, file_name, file_path_or_url, mime_type, file_size_bytes, thumbnail_url, duration_seconds, width, height, uploaded_at , transferId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, uploadedByUserId != 0 ? uploadedByUserId : 0);
            statement.setString(2, fileName);
            statement.setString(3, filePathOrUrl);
            statement.setString(4, mediaType);
            statement.setLong(5, fileSize);
            statement.setString(6, thumbnailUrl);
            statement.setObject(7, durationSeconds);
            statement.setObject(8, width);
            statement.setObject(9, height);
            statement.setTimestamp(10, uploadedAt);
            statement.setString(1, transferId);

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
        String sql = "UPDATE media SET uploader_user_id = ?, file_name = ?, file_path_or_url = ?, mime_type = ?, file_size_bytes = ?, thumbnail_url = ?, duration_seconds = ?, width = ?, height = ?, transferId = ? WHERE media_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, uploadedByUserId != 0 ? uploadedByUserId : 0);
            statement.setString(2, fileName);
            statement.setString(3, filePathOrUrl);
            statement.setString(4, mediaType);
            statement.setLong(5, fileSize);
            statement.setString(6, thumbnailUrl);
            statement.setObject(7, durationSeconds);
            statement.setObject(8, width);
            statement.setObject(9, height);
            statement.setLong(10, id);
            statement.setString(11, transferId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete() throws SQLException {
        String sql = "DELETE FROM media WHERE media_id = ?";
        try (PreparedStatement statement = DatabaseConnectionSQLite.getInstance().getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public String toString() {
        return "Media{" +
                "mediaId=" + id +
                ", uploaderUserId=" + uploadedByUserId +
                ", fileName='" + fileName + '\'' +
                ", filePathOrUrl='" + filePathOrUrl + '\'' +
                ", mimeType='" + mediaType + '\'' +
                ", fileSizeBytes=" + fileSize +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", width=" + width +
                ", height=" + height +
                ", uploadedAt=" + uploadedAt +
                ", transferId='" + transferId + '\'' +
                '}';
    }
}