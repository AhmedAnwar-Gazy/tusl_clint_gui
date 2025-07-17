package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.Message;

import java.util.List;

/**
 * Listener for retrieving lists of messages for a chat.
 */
public interface OnMessagesRetrievedListener {
    void onMessagesRetrieved(List<Message> messages, int chatId);
}
