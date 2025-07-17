package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.Message;

/**
 * Listener for new messages received from the server (unsolicited).
 */
public interface OnNewMessageListener {
    void onNewMessageReceived(Message message);
}
