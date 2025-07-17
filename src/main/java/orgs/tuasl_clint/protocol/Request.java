// src/orgs/protocol/Request.java
package orgs.tuasl_clint.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import orgs.tuasl_clint.models2.Chat;
import orgs.tuasl_clint.models2.ChatParticipant;
import orgs.tuasl_clint.utils.ChatParticipantsRoleAdapter;
import orgs.tuasl_clint.utils.ChatTypeAdapter;
import orgs.tuasl_clint.utils.TimestampAdapter;
import orgs.tuasl_clint.utils.LocalDateTimeAdapter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class Request {
    private Command command;
    private String payload;// Stores the JSON string representation of the data object

    // Use a static Gson instance for consistent serialization
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Timestamp.class, new TimestampAdapter())
            .registerTypeAdapter(Chat.ChatType.class, new ChatTypeAdapter())
            .registerTypeAdapter(ChatParticipant.ChatParticipantRole.class, new ChatParticipantsRoleAdapter())
            .serializeNulls()
            .create();

    public Request(Command command, Map<String, Object> data) {
        this.command = command;
        // Serialize the data map into a JSON string for the payload
        this.payload = gson.toJson(data);
    }

    // Constructor for requests without a specific data payload (e.g., logout, get_all_users)
    public Request(Command command) {
        this.command = command;
        this.payload = null; // Or an empty JSON object string "{}"
    }

    // Getters for deserialization on the server side
    public Command getCommand() {
        return command;
    }

    public String getPayload() {
        return payload;
    }

    // Method for the server to easily get the payload as a Map
    public Map<String, Object> getPayloadAsMap() {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        return gson.fromJson(payload, Map.class);
    }

    // For debugging/logging
    @Override
    public String toString() {
        return "Request{" +
                "command='" + command + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}