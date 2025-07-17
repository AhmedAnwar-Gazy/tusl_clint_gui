package orgs.tuasl_clint.utils;

import com.google.gson.*;
import orgs.tuasl_clint.models2.Chat;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatTypeAdapter implements JsonSerializer<Chat.ChatType>, JsonDeserializer<Chat.ChatType> {


    @Override
    public JsonElement serialize(Chat.ChatType src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(switch (src){
            case CHANNEL -> "channel";
            case GROUP -> "group";
            default -> "private";
        });
    }

    @Override
    public Chat.ChatType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        String dateStr = json.getAsString();

        return switch (dateStr){
            case "channel" -> Chat.ChatType.CHANNEL;
            case "group" -> Chat.ChatType.GROUP;
            default -> Chat.ChatType.PRIVATE;
        };
    }
}
