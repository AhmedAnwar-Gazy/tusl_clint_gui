package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.ChatParticipant;

import java.util.List;

/**
 * Listener for retrieving a list of chat participants.
 */
public interface OnChatParticipantsRetrievedListener {
    void onChatParticipantsRetrieved(List<ChatParticipant> participants, int chatId);
}
