package orgs.tuasl_clint.client;

import orgs.tuasl_clint.models2.User;

/**
 * Listener for retrieving a single User object (e.g., by ID or phone number).
 */
public interface OnUserRetrievedListener {
    void onUserRetrieved(User user);
}
