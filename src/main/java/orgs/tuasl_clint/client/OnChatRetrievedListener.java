package orgs.tuasl_clint.client;
import orgs.tuasl_clint.models2.Chat;

/**
 * Listener for retrieving a single Chat object by its ID.
 */
public interface OnChatRetrievedListener {
    void onChatRetrieved(Chat chat);
}


