package orgs.tuasl_clint.utils;

import com.google.gson.*;
import orgs.tuasl_clint.models2.Chat;
import orgs.tuasl_clint.models2.ChatParticipant;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

public class ChatParticipantsRoleAdapter implements JsonSerializer<ChatParticipant.ChatParticipantRole>, JsonDeserializer<ChatParticipant.ChatParticipantRole> {

    @Override
    public JsonElement serialize(ChatParticipant.ChatParticipantRole src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(switch (src){
            case ADMIN -> "admin";
            case CREATOR -> "creator";
            case MODERATOR -> "subscriber";
            default -> "member";
        });
    }

    @Override
    public ChatParticipant.ChatParticipantRole deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        String dateStr = json.getAsString();

        return switch (dateStr){
            case "admin" -> ChatParticipant.ChatParticipantRole.ADMIN;
            case "creator" -> ChatParticipant.ChatParticipantRole.CREATOR;
            case "subscriber" -> ChatParticipant.ChatParticipantRole.MODERATOR;
            default -> ChatParticipant.ChatParticipantRole.MEMBER;
        };
    }
}
