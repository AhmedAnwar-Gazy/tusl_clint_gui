package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.Chat;

import java.util.List;

/**
 * Listener for retrieving the current user's chats.
 */
public interface OnUserChatsRetrievedListener {
    void onUserChatsRetrieved(List<Chat> chats);
}

